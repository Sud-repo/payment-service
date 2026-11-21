package com.dev.payment.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.payment.entity.ApiKey;
import com.dev.payment.entity.Merchant;
import com.dev.payment.enums.ApiKeyPermission;
import com.dev.payment.enums.Environment;
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
	public ResponseEntity<ApiResponse<ApiDetailsResponse>> generateApiKeys(UUID merchantId, Environment environment) {
		if (merchantId == null || environment == null) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), Constants.MERCHANT_ID_REQUIRED, null));
		}

		// 1. check whether merchant exists and lock the merchant row
		Merchant merchant = merchantRepository.findByIdForUpdate(merchantId)
				.orElseThrow(() -> new ResourceNotFoundException(
						Constants.MERCHANT_NOT_FOUND
				));

		// 2. check if environment - API keys issued for the merchant
		if (apiKeyRepository.existsByMerchantIdAndEnvironment(merchantId, environment)) {
			return ResponseEntity
					.status(HttpStatus.CONFLICT)
					.body(new ApiResponse<>(false, HttpStatus.CONFLICT.value(), Constants.MERCHANT_API_KEY_EXISTS, null));
		}

		// 3. check if merchant status is not approved
		if (merchant.getStatus() != MerchantStatus.APPROVED &&
			    merchant.getStatus() != MerchantStatus.ACTIVE) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), Constants.MERCHANT_NOT_APPROVED, null));
		}

		// 4. create 2 ApiKey entities:
		// 1st one with 2 permissions (read and write)
		// 2nd one with 1 permission (read)
		List<Set<ApiKeyPermission>> permissionSets = List.of(
			    EnumSet.of(
			        ApiKeyPermission.PAYMENT_CREATE,
			        ApiKeyPermission.PAYMENT_READ
			    ),
			    EnumSet.of(
			        ApiKeyPermission.PAYMENT_READ
			    )
			);

		List<ApiKeyResponse> apiKeys = new ArrayList<>();

		for (Set<ApiKeyPermission> permissions : permissionSets) {
			// generate raw api key
			String rawApiKey = ApiKeyUtils.generateApiKey();

			// get prefix
			String prefix = ApiKeyUtils.getApiKeyPrefix(merchant.getName(), environment);

			// api key with prefix
			String fullApiKey = prefix + rawApiKey;

			// extract prefix key to store in DB
			String keyPrefix = ApiKeyUtils.extractKeyPrefix(fullApiKey, environment);

			// hash only the api key without prefix
			String keyHash = ApiKeyUtils.hashApiKey(rawApiKey);

			// store only the hash and key prefix
			ApiKey apiKey = new ApiKey();
			apiKey.setMerchant(merchant);
			apiKey.setKeyPrefix(keyPrefix);
			apiKey.setKeyHash(keyHash);
			apiKey.setIsActive(true);
			apiKey.setEnvironment(environment);
			apiKey.setPermissions(permissions);

			apiKeyRepository.save(apiKey);

			ApiKeyResponse apiKeyResponse = new ApiKeyResponse();
			// return the apikey with prefix to the client
			apiKeyResponse.setApiKey(fullApiKey);
			apiKeyResponse.setPermissions(permissions);

			apiKeys.add(apiKeyResponse);
		}
		ApiDetailsResponse apiDetailsResponse = new ApiDetailsResponse(Constants.DOMAIN, 
				Constants.CREATE_PAYMENT_ENDPOINT, Constants.READ_PAYMENT_ENDPOINT, apiKeys);
		
		merchant.setStatus(MerchantStatus.ACTIVE);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(new ApiResponse<>(true, HttpStatus.CREATED.value(), Constants.API_KEYS_GENERATED_SUCCESS, apiDetailsResponse));
	}

}

