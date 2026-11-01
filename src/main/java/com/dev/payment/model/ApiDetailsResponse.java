package com.dev.payment.model;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
public class ApiDetailsResponse {
	
	private String domain;
	private String createPaymentEndpoint;
	private String readPaymentsEndpoint;
	private List<ApiKeyResponse> apiKeys;


}
