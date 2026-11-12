package com.dev.payment.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiDetailsResponse {
	
	private String domain;
	private String createPaymentEndpoint;
	private String readPaymentsEndpoint;
	private List<ApiKeyResponse> apiKeys;


}
