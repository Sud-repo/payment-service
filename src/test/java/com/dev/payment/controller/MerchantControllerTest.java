package com.dev.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.dev.payment.enums.MerchantStatus;
import com.dev.payment.model.ApiResponse;
import com.dev.payment.model.MerchantRequest;
import com.dev.payment.model.MerchantResponse;
import com.dev.payment.service.MerchantService;
import com.dev.payment.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MerchantControllerTest {

	private MockMvc mockMvc;

	@Mock
	private MerchantService merchantService;

	@InjectMocks
	private MerchantController merchantController;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(merchantController)
				.setValidator(validator)
				.build();
		objectMapper = new ObjectMapper();
	}

	@Test
	void registerMerchant_shouldReturnCreated_whenPayloadIsValid() throws Exception {
		UUID merchantId = UUID.randomUUID();
		MerchantRequest request = new MerchantRequest("Test Merchant", "test@merchant.com");
		MerchantResponse responseDto = new MerchantResponse(
				merchantId, "Test Merchant", "test@merchant.com", MerchantStatus.REGISTERED);

		ApiResponse<MerchantResponse> apiResponse = new ApiResponse<>(
				true, HttpStatus.CREATED.value(), Constants.MERCHANT_REGISTERED_SUCCESS, responseDto);

		when(merchantService.registerMerchant(any(MerchantRequest.class)))
				.thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));

		mockMvc.perform(post("/admin/v1/merchants")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.statusCode").value(201))
				.andExpect(jsonPath("$.message").value(Constants.MERCHANT_REGISTERED_SUCCESS))
				.andExpect(jsonPath("$.data.id").value(merchantId.toString()))
				.andExpect(jsonPath("$.data.name").value("Test Merchant"))
				.andExpect(jsonPath("$.data.email").value("test@merchant.com"))
				.andExpect(jsonPath("$.data.status").value("REGISTERED"));

		verify(merchantService).registerMerchant(any(MerchantRequest.class));
	}

	@Test
	void registerMerchant_shouldReturnConflict_whenNameOrEmailExists() throws Exception {
		MerchantRequest request = new MerchantRequest("Existing Merchant", "existing@merchant.com");

		ApiResponse<MerchantResponse> apiResponse = new ApiResponse<>(
				false, HttpStatus.CONFLICT.value(), Constants.MERCHANT_NAME_EXISTS, null);

		when(merchantService.registerMerchant(any(MerchantRequest.class)))
				.thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse));

		mockMvc.perform(post("/admin/v1/merchants")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.statusCode").value(409))
				.andExpect(jsonPath("$.message").value(Constants.MERCHANT_NAME_EXISTS));
	}

	@Test
	void registerMerchant_shouldReturnBadRequest_whenNameIsTooShort() throws Exception {
		MerchantRequest request = new MerchantRequest("AB", "valid@email.com");

		mockMvc.perform(post("/admin/v1/merchants")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void registerMerchant_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
		MerchantRequest request = new MerchantRequest("Valid Name", "invalid-email");

		mockMvc.perform(post("/admin/v1/merchants")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateMerchantStatus_shouldReturnOk_whenStatusIsApproved() throws Exception {
		UUID merchantId = UUID.randomUUID();
		MerchantResponse response = new MerchantResponse(
				merchantId, "Test Merchant", "test@merchant.com", MerchantStatus.APPROVED);

		ApiResponse<MerchantResponse> apiResponse = new ApiResponse<>(
				true, HttpStatus.OK.value(), Constants.MERCHANT_STATUS_UPDATED_SUCCESS, response);

		when(merchantService.updateMerchantStatus(eq(merchantId), eq(MerchantStatus.APPROVED)))
				.thenReturn(ResponseEntity.ok(apiResponse));

		mockMvc.perform(patch("/admin/v1/merchants/" + merchantId + "/status")
				.param("status", "APPROVED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.statusCode").value(200))
				.andExpect(jsonPath("$.message").value(Constants.MERCHANT_STATUS_UPDATED_SUCCESS))
				.andExpect(jsonPath("$.data.status").value("APPROVED"));

		verify(merchantService).updateMerchantStatus(eq(merchantId), eq(MerchantStatus.APPROVED));
	}



	@Test
	void updateMerchantStatus_shouldReturnNotFound_whenMerchantDoesNotExist() throws Exception {
		UUID merchantId = UUID.randomUUID();

		ApiResponse<MerchantResponse> apiResponse = new ApiResponse<>(
				false, HttpStatus.NOT_FOUND.value(), Constants.MERCHANT_NOT_FOUND, null);

		when(merchantService.updateMerchantStatus(eq(merchantId), eq(MerchantStatus.APPROVED)))
				.thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse));

		mockMvc.perform(patch("/admin/v1/merchants/" + merchantId + "/status")
				.param("status", "APPROVED"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.statusCode").value(404))
				.andExpect(jsonPath("$.message").value(Constants.MERCHANT_NOT_FOUND));
	}

	@Test
	void updateMerchantStatus_shouldReturnBadRequest_whenStatusIsInvalid() throws Exception {
		UUID merchantId = UUID.randomUUID();

		ApiResponse<MerchantResponse> apiResponse = new ApiResponse<>(
				false, HttpStatus.BAD_REQUEST.value(), Constants.INVALID_MERCHANT_STATUS, null);

		when(merchantService.updateMerchantStatus(eq(merchantId), eq(MerchantStatus.REGISTERED)))
				.thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse));

		mockMvc.perform(patch("/admin/v1/merchants/" + merchantId + "/status")
				.param("status", "REGISTERED"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.statusCode").value(400))
				.andExpect(jsonPath("$.message").value(Constants.INVALID_MERCHANT_STATUS));
	}

}
