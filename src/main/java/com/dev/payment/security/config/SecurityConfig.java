package com.dev.payment.security.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.dev.payment.util.Constants;
import com.nimbusds.jose.jwk.source.ImmutableSecret;


@Configuration
public class SecurityConfig {

    private final ApiKeyAuthFilter apiKeyAuthFilter;

    private final InvalidUserAuthEntryPoint authEntryPoint;

    private final AccessDeniedExceptionHandler accessDeniedExceptionHandler;

    public SecurityConfig(ApiKeyAuthFilter apiKeyAuthFilter, 
    		InvalidUserAuthEntryPoint authEntryPoint, AccessDeniedExceptionHandler accessDeniedExceptionHandler) {
        this.apiKeyAuthFilter = apiKeyAuthFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedExceptionHandler = accessDeniedExceptionHandler;
    }

    public static PathPatternRequestMatcher[] WHITELISTED_URLS = {
			PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/admin/login"),
	};
	
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 1. Admin endpoints: jwt authentication
    @Bean
    @Order(1)
    SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {

    	return http
                .securityMatcher("/admin/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                		.requestMatchers(WHITELISTED_URLS).permitAll()
                		// add Authorization conditions later...
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
	                oauth2.jwt(
	                    org.springframework.security.config.Customizer
	                        .withDefaults()
	                )
	            )
//                .addFilterBefore( >> Oauth Resource Server will handle JWT Verification
//                        apiKeyAuthFilter,
//                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(handler -> handler
                		.accessDeniedHandler(accessDeniedExceptionHandler)
                		.authenticationEntryPoint(authEntryPoint)
                		)
                .build();
    }

    // 2. Payment endpoints: API key authentication
    @Bean
    @Order(2)
    SecurityFilterChain paymentSecurity(HttpSecurity http) throws Exception {

        return http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .addFilterBefore(
                        apiKeyAuthFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(handler -> handler
                		.authenticationEntryPoint(authEntryPoint)
                		)
                .build();
    }

    // 3. Deny requests that don't match either chain
    @Bean
    @Order(3)
    SecurityFilterChain fallbackSecurity(HttpSecurity http) throws Exception {

        return http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().denyAll())
                .exceptionHandling(handler -> handler
                		.authenticationEntryPoint(authEntryPoint)
                		)
                .build();
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecretKey jwtSecretKey(
            @Value("${jwt.secret.key}") String secret) {

        byte[] keyBytes = Base64.getDecoder().decode(secret);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 bytes");
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(
            new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey, @Value("${spring.application.name}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withSecretKey(jwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
            JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
            jwt.getAudience().contains(Constants.AUDIENCE)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token")
                );

        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                issuerValidator,
                audienceValidator
            )
        );

        return decoder;
    }


    @Bean // disable servlet-container (Tomcat) registration
    FilterRegistrationBean<ApiKeyAuthFilter> apiKeyFilterRegistration(ApiKeyAuthFilter filter) {
        FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

}

