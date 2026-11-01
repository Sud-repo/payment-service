package com.dev.payment.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dev.payment.model.ApiDetailsResponse;
import com.dev.payment.model.ApiResponse;
import com.dev.payment.service.ApiKeyService;

@RestController
@RequestMapping("/admin/v1/apikeys")
public class ApiKeyController {

	private final ApiKeyService apiKeyService;

	public ApiKeyController(ApiKeyService apiKeyService) {
		this.apiKeyService = apiKeyService;
	}

	@PostMapping("/generate")
	public ResponseEntity<ApiResponse<ApiDetailsResponse>> createApiKeys(@RequestParam UUID merchantId) {
		return apiKeyService.generateApiKeys(merchantId);
	}

}

