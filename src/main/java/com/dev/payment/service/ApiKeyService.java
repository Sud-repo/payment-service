package com.dev.payment.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.payment.entity.ApiKey;
import com.dev.payment.entity.Merchant;
import com.dev.payment.enums.MerchantStatus;
import com.dev.payment.exception.ResourceNotFoundException;
import com.dev.payment.model.ApiDetailsResponse;
import com.dev.payment.model.ApiKeyResponse;
import com.dev.payment.model.ApiResponse;
import com.dev.payment.repository.ApiKeyRepository;
import com.dev.payment.repository.MerchantRepository;
import com.dev.payment.util.ApiKeyUtils;
import com.dev.payment.util.Constants;

@Service
public class ApiKeyService {

	private final ApiKeyRepository apiKeyRepository;
	private final MerchantRepository merchantRepository;

	public ApiKeyService(ApiKeyRepository apiKeyRepository, MerchantRepository merchantRepository) {
		this.apiKeyRepository = apiKeyRepository;
		this.merchantRepository = merchantRepository;
	}

	@Transactional
	public ResponseEntity<ApiResponse<ApiDetailsResponse>> generateApiKeys(UUID merchantId) {
		if (merchantId == null) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), Constants.MERCHANT_ID_REQUIRED, null));
		}

		// 1. check whether merchant exists and lock the merchant row
		Merchant merchant = merchantRepository.findByIdForUpdate(merchantId)
				.orElseThrow(() -> new ResourceNotFoundException(
						Constants.MERCHANT_NOT_FOUND
				));

		// 2. check if API keys issued for the merchant
		if (merchant.getStatus() == MerchantStatus.ISSUED_API) {
			return ResponseEntity
					.status(HttpStatus.CONFLICT)
					.body(new ApiResponse<>(false, HttpStatus.CONFLICT.value(), Constants.MERCHANT_API_KEY_EXISTS, null));
		}

		// 3. check if merchant status is not approved
		if (merchant.getStatus() != MerchantStatus.APPROVED) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), Constants.MERCHANT_NOT_APPROVED, null));
		}

		// 4. create 2 ApiKey entities:
		// 1st one with 2 permissions (read and write)
		// 2nd one with 1 permission (read)
		List<Set<String>> permissionSets = Arrays.asList(
				new LinkedHashSet<>(Arrays.asList(Constants.CREATE_PERMISSION, Constants.READ_PERMISSION)),
				new LinkedHashSet<>(Collections.singletonList(Constants.READ_PERMISSION))
		);

		List<ApiKeyResponse> apiKeys = new ArrayList<>();

		for (Set<String> permissions : permissionSets) {
			// generate raw api key
			String rawApiKey = ApiKeyUtils.generateApiKey();

			// get prefix
			String prefix = ApiKeyUtils.getApiKeyPrefix(merchant.getName());

			// api key with prefix
			String fullApiKey = prefix + rawApiKey;

			// extract prefix key to store in DB
			String keyPrefix = ApiKeyUtils.extractKeyPrefix(fullApiKey);

			// hash only the api key without prefix
			String keyHash = ApiKeyUtils.hashApiKey(rawApiKey);

			// store only the hash and key prefix
			ApiKey apiKey = new ApiKey();
			apiKey.setMerchant(merchant);
			apiKey.setKeyPrefix(keyPrefix);
			apiKey.setKeyHash(keyHash);
			apiKey.setIsActive(true);
			apiKey.setPermission(permissions);

			apiKeyRepository.save(apiKey);

			// return the apikey with prefix to the client
			ApiKeyResponse apiKeyResponse = new ApiKeyResponse();
			apiKeyResponse.setApiKey(fullApiKey);
			apiKeyResponse.setPermissions(permissions);

			apiKeys.add(apiKeyResponse);
		}
		ApiDetailsResponse apiDetailsResponse = new ApiDetailsResponse(Constants.DOMAIN, 
				Constants.CREATE_PAYMENT_ENDPOINT, Constants.READ_PAYMENT_ENDPOINT, apiKeys);
		
		merchant.setStatus(MerchantStatus.ISSUED_API);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(new ApiResponse<>(true, HttpStatus.CREATED.value(), Constants.API_KEYS_GENERATED_SUCCESS, apiDetailsResponse));
	}

}

