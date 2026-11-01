package com.dev.payment.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dev.payment.model.ApiDetailsResponse;
import com.dev.payment.model.ApiKeyResponse;
import com.dev.payment.model.ApiResponse;
import com.dev.payment.service.ApiKeyService;
import com.dev.payment.util.Constants;

@ExtendWith(MockitoExtension.class)
class ApiKeyControllerTest {

	private MockMvc mockMvc;

	@Mock
	private ApiKeyService apiKeyService;

	@InjectMocks
	private ApiKeyController apiKeyController;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(apiKeyController).build();
	}


	@Test
	void createApiKeys_shouldReturnCreated_withMerchantIdCamelCaseParam() throws Exception {
		UUID merchantId = UUID.randomUUID();

		ApiKeyResponse key1 = new ApiKeyResponse("ps_mer_Acmkey123", Set.of("read", "write"));
		ApiKeyResponse key2 = new ApiKeyResponse("ps_mer_Acmkey456", Set.of("read"));
		
		ApiDetailsResponse response = new ApiDetailsResponse("", "", "", Arrays.asList(key1, key2));

		ApiResponse<ApiDetailsResponse> apiResponse = new ApiResponse<>(
				true, HttpStatus.CREATED.value(), Constants.API_KEYS_GENERATED_SUCCESS, response);

		when(apiKeyService.generateApiKeys(eq(merchantId)))
				.thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));

		mockMvc.perform(post("/admin/v1/apikeys/generate")
				.param("merchantId", merchantId.toString()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.statusCode").value(201));

		verify(apiKeyService).generateApiKeys(eq(merchantId));
	}



}

