package com.dev.payment.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import com.dev.payment.enums.Environment;

import static com.dev.payment.util.Constants.TEST_API_KEY_PREFIX;
import static com.dev.payment.util.Constants.PROD_API_KEY_PREFIX;;


public class ApiKeyUtils {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

    public static boolean validateApiKeyHash(String requestApiKey, String storedApiKeyHash, Environment environment) {
    	String apiKey = removeApiKeyPrefix(requestApiKey, environment);
        String apiKeyHash = hashApiKey(apiKey);
        return MessageDigest.isEqual(
    		apiKeyHash.getBytes(StandardCharsets.US_ASCII),
    		storedApiKeyHash.getBytes(StandardCharsets.US_ASCII));
    }
    
    private static String getEnvApiKeyPrefix(Environment environment) {
    	return environment == Environment.PROD ? 
    			PROD_API_KEY_PREFIX : TEST_API_KEY_PREFIX;
    }

    public static String getApiKeyPrefix(String merchantName, Environment environment) {
    	return getEnvApiKeyPrefix(environment) + merchantName.substring(0, Constants.MERCHANT_PREFIX_LENGTH);
    }

    private static int getApiKeyPrefixLength(Environment environment) {
    	return getEnvApiKeyPrefix(environment).length() + Constants.MERCHANT_PREFIX_LENGTH;
    }
    
    public static String extractKeyPrefix(String apiKey, Environment environment) throws IllegalArgumentException {
        int prefixLength = getApiKeyPrefixLength(environment) + Constants.Key_PREFIX_LENGTH;

        if (apiKey.length() < prefixLength) {
            throw new IllegalArgumentException("Invalid API key format");
        }

        return apiKey.substring(0, prefixLength);
    }
    

    private static String removeApiKeyPrefix(String providedApiKey, Environment environment) {
        return providedApiKey.substring(getApiKeyPrefixLength(environment));
    }
    
}
