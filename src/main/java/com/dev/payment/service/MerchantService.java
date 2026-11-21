package com.dev.payment.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.dev.payment.entity.Merchant;
import com.dev.payment.enums.MerchantStatus;
import com.dev.payment.exception.ResourceNotFoundException;
import com.dev.payment.model.ApiPageResponse;
import com.dev.payment.model.ApiResponse;
import com.dev.payment.model.MerchantRequest;
import com.dev.payment.model.MerchantResponse;
import com.dev.payment.repository.MerchantRepository;
import com.dev.payment.security.entity.User;
import com.dev.payment.security.service.LoginService;
import com.dev.payment.util.Constants;

@Service
public class MerchantService {

	private final MerchantRepository merchantRepository;
	private final LoginService loginService;

	public MerchantService(MerchantRepository merchantRepository, LoginService loginService) {
		this.merchantRepository = merchantRepository;
		this.loginService = loginService;
	}

	public ResponseEntity<ApiResponse<MerchantResponse>> registerMerchant(MerchantRequest merchantRequest) {
		// 1. check duplicate name or email
		if (merchantRepository.existsByName(merchantRequest.getName())) {
			return ResponseEntity
					.status(HttpStatus.CONFLICT.value())
					.body(new ApiResponse<MerchantResponse>(false, HttpStatus.CONFLICT.value(), Constants.MERCHANT_NAME_EXISTS, null));
		}

		if (loginService.existsByEmail(merchantRequest.getEmail())) {
			return ResponseEntity
					.status(HttpStatus.CONFLICT.value())
					.body(new ApiResponse<MerchantResponse>(false, HttpStatus.CONFLICT.value(), Constants.MERCHANT_EMAIL_EXISTS, null));
		}

		// 2. create user and merchant entity
		User user = loginService.createMerchantUser(merchantRequest);
		
		Merchant merchant = new Merchant();
		merchant.setName(merchantRequest.getMerchantName());
		merchant.setStatus(MerchantStatus.REGISTERED);
		merchant.setUser(user);

		Merchant savedMerchant = merchantRepository.save(merchant);

		// 3. create a response model
		MerchantResponse responseModel = new MerchantResponse();
		responseModel.setId(savedMerchant.getId());
		responseModel.setName(savedMerchant.getName());
		responseModel.setEmail(user.getUserName());
		responseModel.setStatus(savedMerchant.getStatus());

		return ResponseEntity
				.status(HttpStatus.CREATED.value())
				.body(new ApiResponse<MerchantResponse>(true, HttpStatus.CREATED.value(), Constants.MERCHANT_REGISTERED_SUCCESS, responseModel));
	}

	public ResponseEntity<ApiResponse<MerchantResponse>> updateMerchantStatus(UUID merchantId, MerchantStatus status) {
		// 1. validate status is approved or rejected
		if (status == null || (status != MerchantStatus.APPROVED && status != MerchantStatus.REJECTED)) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST.value())
					.body(new ApiResponse<MerchantResponse>(false, HttpStatus.BAD_REQUEST.value(), Constants.INVALID_MERCHANT_STATUS, null));
		}

		// 2. check whether merchant exists
		Merchant merchant = merchantRepository.findById(merchantId)
				.orElseThrow(() -> new ResourceNotFoundException(
		        		Constants.MERCHANT_NOT_FOUND
		        ));

		// 3. update status
		merchant.setStatus(status);
		Merchant updatedMerchant = merchantRepository.save(merchant);
		User user = merchant.getUser();
		loginService.updateUserStatus(user, 
				status == MerchantStatus.APPROVED ? true : false);

		// 4. create response model
		MerchantResponse responseModel = new MerchantResponse();
		responseModel.setId(updatedMerchant.getId());
		responseModel.setName(updatedMerchant.getName());
		responseModel.setEmail(user.getUserName());
		responseModel.setStatus(updatedMerchant.getStatus());

		return ResponseEntity
				.status(HttpStatus.OK.value())
				.body(new ApiResponse<MerchantResponse>(true, HttpStatus.OK.value(), Constants.MERCHANT_STATUS_UPDATED_SUCCESS, responseModel));
	}

	public ResponseEntity<ApiPageResponse<MerchantResponse>>  getMerchants(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Direction.ASC, "name"));
		Page<MerchantResponse> pageResponse = merchantRepository.findAll(pageable)
		        .map(merchant -> new MerchantResponse(
		        	merchant.getId(),
		            merchant.getName(),
		            null,
		            merchant.getStatus()
		        ));
		
		return ResponseEntity.ok(new ApiPageResponse<>(
	            pageResponse.getContent(),
	            pageResponse.getNumber(),
	            pageResponse.getSize(),
	            pageResponse.getTotalElements(),
	            pageResponse.getTotalPages(),
	            pageResponse.isLast()
	    ));
		
	}
	
	public ResponseEntity<ApiResponse<MerchantResponse>> getMerchantById(UUID merchantId) {
		Merchant merchant = merchantRepository.findById(merchantId)
		        .orElseThrow(() -> new ResourceNotFoundException(
		        		Constants.MERCHANT_NOT_FOUND
		        ));
	    
	    MerchantResponse response = new MerchantResponse(
		        merchant.getId(),
		        merchant.getName(),
		        null,
		        merchant.getStatus()
		    );

	    return ResponseEntity
	    		.ok(new ApiResponse<MerchantResponse>(
	    				true, 
	    				HttpStatus.OK.value(), 
	    				"Successful", 
	    				response
				));
	}

}
