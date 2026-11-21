package com.dev.payment.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MerchantContext {

	private UUID merchantId;
	private String merchantName;
	private String email;

}
