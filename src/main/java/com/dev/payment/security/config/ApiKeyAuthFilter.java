package com.dev.payment.security.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dev.payment.entity.ApiKey;
import com.dev.payment.entity.Merchant;
import com.dev.payment.model.MerchantContext;
import com.dev.payment.repository.ApiKeyRepository;
import com.dev.payment.security.entity.User;
import com.dev.payment.util.ApiKeyUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

	private static final String API_KEY_HEADER = "api-key";

	private final ApiKeyRepository apiKeyRepository;

	public static final ThreadLocal<MerchantContext> MERCHANT_CONTEXT = new ThreadLocal<>();

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    	String requestApiKey = request.getHeader(API_KEY_HEADER);
    	String merchantName = request.getHeader("merchant");

        if (!StringUtils.hasText(requestApiKey) || !StringUtils.hasText(merchantName)
        		|| !requestApiKey.startsWith(ApiKeyUtils.getApiKeyPrefix(merchantName))) {
            writeUnauthorized(response);
            return;
        }

        final String keyPrefix;

        try {
            keyPrefix = ApiKeyUtils.extractKeyPrefix(requestApiKey);

	        Optional<ApiKey> optApiKey =
	                apiKeyRepository.findByKeyPrefixAndIsActiveTrue(keyPrefix);
	
	        if (optApiKey.isEmpty()) {
	            writeUnauthorized(response);
	            return;
	        }
	
	        ApiKey apiKey = optApiKey.get();
	        Merchant merchant = apiKey.getMerchant();
	        User user = merchant.getUser();
	
	        if (!isValidApiKey(requestApiKey, apiKey) 
	        		|| !merchant.getName().equals(merchantName) || !user.getIsActive()) {
	            writeUnauthorized(response);
	            return;
	        }
	        MERCHANT_CONTEXT.set(new MerchantContext(
	        		merchant.getId(), 
	        		merchant.getName(), 
	        		user.getUserName()));
        } catch (IllegalArgumentException ex) {
            writeUnauthorized(response);
            return;
        }
        filterChain.doFilter(request, response);

    }
    
    private boolean isValidApiKey(String requestApiKey, ApiKey apiKeyEntity) {
    	if (!apiKeyEntity.getIsActive() ||
//    			apiKeyEntity.getExpiresAt().after(new Date()) ||
    			apiKeyEntity.getPermission().isEmpty()) {
    		return false;
    	}

    	String apiKey = ApiKeyUtils.removeApiKeyPrefix(requestApiKey);
        String apiKeyHash = ApiKeyUtils.hashApiKey(apiKey);
        return MessageDigest.isEqual(
    		apiKeyHash.getBytes(StandardCharsets.US_ASCII),
    		apiKeyEntity.getKeyHash().getBytes(StandardCharsets.US_ASCII)
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
