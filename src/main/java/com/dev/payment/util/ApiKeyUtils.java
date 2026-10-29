package com.dev.payment.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.util.StringUtils;
import static com.dev.payment.util.Constants.API_KEY_PREFIX;


public class ApiKeyUtils {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final int MERCHANT_PREFIX_LENGTH = 3;


    public static String generateApiKey() {
        byte[] key = new byte[32]; // 256 bits
        SECURE_RANDOM.nextBytes(key);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(key);
    }
    
    
    public static String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
    
    public static String getApiKeyPrefix(String merchantName) {
    	return API_KEY_PREFIX + merchantName.substring(0, MERCHANT_PREFIX_LENGTH);
    }

    private static int getApiKeyPrefixLength() {
    	return API_KEY_PREFIX.length() + MERCHANT_PREFIX_LENGTH;
    }
    
    public static String extractKeyPrefix(String apiKey) throws IllegalArgumentException {
        int prefixLength = getApiKeyPrefixLength() + 8;

        if (apiKey.length() < prefixLength) {
            throw new IllegalArgumentException("Invalid API key format");
        }

        return apiKey.substring(0, prefixLength);
    }
    

    public static String removeApiKeyPrefix(String providedApiKey) {
        return providedApiKey.substring(getApiKeyPrefixLength());
    }
    
}
