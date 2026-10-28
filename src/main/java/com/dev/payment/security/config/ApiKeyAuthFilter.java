package com.dev.payment.security.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dev.payment.entity.ApiKey;
import com.dev.payment.repository.ApiKeyRepository;
import com.dev.payment.security.repository.UserRepository;
import com.dev.payment.util.ApiKeyUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

	private static final String API_KEY_HEADER = "X-API-KEY";

	private final ApiKeyRepository apiKeyRepository;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    	String providedApiKey = request.getHeader(API_KEY_HEADER);
    	String merchantName = request.getHeader("merchant");

        if (!StringUtils.hasText(providedApiKey) || !StringUtils.hasText(merchantName)
        		|| !providedApiKey.startsWith(ApiKeyUtils.getApiKeyPrefix(merchantName))) {
            writeUnauthorized(response);
            return;
        }

        final String keyPrefix;

        try {
            keyPrefix = ApiKeyUtils.extractKeyPrefix(providedApiKey);

	        Optional<ApiKey> optApiKey =
	                apiKeyRepository.findByKeyPrefixAndIsActiveTrue(keyPrefix);
	
	        if (optApiKey.isEmpty()) {
	            writeUnauthorized(response);
	            return;
	        }
	
	        ApiKey apiKey = optApiKey.get();
	
	        if (!isValidApiKey(providedApiKey, apiKey.getKeyHash()) 
	        		|| !apiKey.getMerchant().getName().equals(merchantName)) {
	            writeUnauthorized(response);
	            return;
	        }
        } catch (IllegalArgumentException ex) {
            writeUnauthorized(response);
            return;
        }
        filterChain.doFilter(request, response);

    }
    
    private boolean isValidApiKey(String providedApiKey, String storedApiKeyHash) {
        String apiKey = ApiKeyUtils.removeApiKeyPrefix(providedApiKey);
        String apiKeyHash = ApiKeyUtils.hashApiKey(apiKey);
        return MessageDigest.isEqual(
    		apiKeyHash.getBytes(StandardCharsets.US_ASCII),
            storedApiKeyHash.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private void writeUnauthorized(
            HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.getWriter().write(
                "{\"success\":false,"
                + "\"statusCode\":401,"
                + "\"message\":\"Invalid or missing API key\"}"
        );
    }

}
