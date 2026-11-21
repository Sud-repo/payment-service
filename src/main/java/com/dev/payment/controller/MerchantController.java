package com.dev.payment.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dev.payment.enums.MerchantStatus;
import com.dev.payment.model.ApiPageResponse;
import com.dev.payment.model.ApiResponse;
import com.dev.payment.model.MerchantRequest;
import com.dev.payment.model.MerchantResponse;
import com.dev.payment.service.MerchantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/admin/v1/merchants")
public class MerchantController {

	private final MerchantService merchantService;

	public MerchantController(MerchantService merchantService) {
		this.merchantService = merchantService;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<MerchantResponse>> registerMerchant(
			@Valid @RequestBody MerchantRequest merchantRequest) {
		return merchantService.registerMerchant(merchantRequest);
	}

	@PatchMapping("/{merchantId}/status")
	public ResponseEntity<ApiResponse<MerchantResponse>> updateMerchantStatus(
			@PathVariable UUID merchantId, @RequestParam MerchantStatus status) {
		return merchantService.updateMerchantStatus(merchantId, status);
	}
	
	@GetMapping
	public ResponseEntity<ApiPageResponse<MerchantResponse>> getMerchants(
			@RequestParam(required = false, defaultValue = "0") int page, 
			@RequestParam(required = false, defaultValue = "10") int size) {
		return merchantService.getMerchants(page, size);
	}
	
	@GetMapping("/{merchantId}")
	public ResponseEntity<ApiResponse<MerchantResponse>> getMerchantById(
	        @PathVariable UUID merchantId) {
	    return merchantService.getMerchantById(merchantId);
	}

}
