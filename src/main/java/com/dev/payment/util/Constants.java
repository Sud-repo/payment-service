package com.dev.payment.util;

public class Constants {
	
	public final static String MERCHANT_ROLE = "MERCHANT";
	
	public final static String ADMIN_ROLE = "ADMIN";
	
	public final static String INVALID_CREDENTIALS = "Invalid Credentials";

	public final static String TEST_API_KEY_PREFIX = "ps_test_";

	public final static String PROD_API_KEY_PREFIX = "ps_live_";

	public final static String AUDIENCE = "admin-api";

	public final static String MERCHANT_NAME_EXISTS = "Merchant name already exists";

	public final static String MERCHANT_EMAIL_EXISTS = "Merchant email already exists";

	public final static String MERCHANT_REGISTERED_SUCCESS = "Merchant registered successfully";

	public final static String MERCHANT_NOT_FOUND = "Merchant not found";

	public final static String MERCHANT_STATUS_UPDATED_SUCCESS = "Merchant status updated successfully";

	public final static String INVALID_MERCHANT_STATUS = "Invalid merchant status. Status must be either APPROVED or REJECTED";

	public final static String MERCHANT_NOT_APPROVED = "Merchant is not approved";

	public final static String API_KEYS_GENERATED_SUCCESS = "API keys generated successfully";

	public final static String MERCHANT_ID_REQUIRED = "Merchant ID is required";
	
	public final static String MERCHANT_API_KEY_EXISTS = "API Keys already Issued";

	public final static String DOMAIN = "http://localhost:8080";

	public final static String CREATE_PAYMENT_ENDPOINT = "api/v1/payments  (POST)";

	public final static String READ_PAYMENT_ENDPOINT = "api/v1/payments  (GET)";

	public final static String PAYMENT_ENDPOINT = "/api/v1/payments";

	public static final int MERCHANT_PREFIX_LENGTH = 3;
	
	public static final int Key_PREFIX_LENGTH = 8;

}
