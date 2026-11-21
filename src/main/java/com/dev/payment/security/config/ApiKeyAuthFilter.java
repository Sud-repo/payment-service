package com.dev.payment.security.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dev.payment.entity.ApiKey;
import com.dev.payment.entity.Merchant;
import com.dev.payment.enums.Environment;
import com.dev.payment.enums.MerchantStatus;
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

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }
    

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    	String requestApiKey = request.getHeader(API_KEY_HEADER);
        if (!StringUtils.hasText(requestApiKey)) {
            writeUnauthorized(response);
            return;
        }

        try {
        	Environment merchantEnvironment = requestApiKey.contains("live") ? Environment.PROD: Environment.TEST;
        	String keyPrefix = ApiKeyUtils.extractKeyPrefix(requestApiKey, merchantEnvironment);
	        Optional<ApiKey> optApiKey =
	                apiKeyRepository.findByKeyPrefixAndIsActiveTrue(keyPrefix);

	        if (optApiKey.isEmpty()) {
	            writeUnauthorized(response);
	            return;
	        }

	        ApiKey apiKey = optApiKey.get();
	        Merchant merchant = apiKey.getMerchant();
	        User user = merchant.getUser();

	        if (!validateApiKey(requestApiKey, apiKey) 
	        		|| !validateMerchant(merchant, user)) {
	            writeUnauthorized(response);
	            return;
	        }

	        MerchantContext merchantContext = new MerchantContext(
	        		merchant.getId(), 
	        		merchant.getName(), 
	        		user.getUserName()
	        );

	        // Set Spring Security Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            merchantContext,
                            null,
                            getAuthorities(apiKey) // handle permission validation
                    );

            SecurityContext securityContext =
                    SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            filterChain.doFilter(request, response);

        } catch (IllegalArgumentException ex) {
            writeUnauthorized(response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }


    private boolean validateMerchant(Merchant merchant, User user) {
    	return merchant.getStatus() == MerchantStatus.ACTIVE
                && Boolean.TRUE.equals(user.getIsActive());
	}


	private boolean validateApiKey(String requestApiKey, ApiKey apiKeyEntity) {
		return Boolean.TRUE.equals(apiKeyEntity.getIsActive())
	            && ApiKeyUtils.validateApiKeyHash(
	                    requestApiKey,
	                    apiKeyEntity.getKeyHash(),
	                    apiKeyEntity.getEnvironment()
	            )
//	            && apiKeyExpiration Check
	            ;

    }

	private Collection<GrantedAuthority> getAuthorities(ApiKey apiKey) {

	    List<GrantedAuthority> authorities = new ArrayList<>();

	    apiKey.getPermissions().forEach(permission ->
	            authorities.add(
	                    new SimpleGrantedAuthority(permission.toString())
	            )
	    );

	    return authorities;
	}

    private void writeUnauthorized(
            HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.getWriter().write(
                "{\"success\":false,"
                + "\"statusCode\":401,"
                + "\"message\":\"Invalid API key\"}"
        );
    }

}
