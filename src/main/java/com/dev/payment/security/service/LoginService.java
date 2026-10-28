package com.dev.payment.security.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.dev.payment.model.ApiResponse;
import com.dev.payment.model.LoginRequest;
import com.dev.payment.model.LoginResponseDto;
import com.dev.payment.security.entity.User;
import com.dev.payment.security.repository.UserRepository;
import com.dev.payment.util.Constants;

@Service
public class LoginService {
	
	private final JwtService jwtService;
	
	private final UserRepository userRepository;

	private final AuthenticationManager authenticationManager;
	
	
	public LoginService(JwtService jwtService, UserRepository userRepository,
			AuthenticationManager authenticationManager) {
		super();
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.authenticationManager = authenticationManager;
	}


	public ResponseEntity<ApiResponse<LoginResponseDto>> authenticateUser(LoginRequest loginRequest) {
		String email = loginRequest.getUserName();
		
		// check whether the user exists
		Optional<User> optUser = userRepository.findByUserName(email);
		// authenticate the user
		if (optUser.isEmpty() || !authenticate(loginRequest)) {
			return ResponseEntity
					.status(HttpStatus.UNAUTHORIZED.value())
					.body(new ApiResponse<LoginResponseDto>(false, 401, Constants.INVALID_CREDENTIALS, null));
		}
		
		User user = optUser.get();
		if (!Boolean.TRUE.equals(user.getIsActive())) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST.value())
					.body(new ApiResponse<LoginResponseDto>(false, 400, Constants.INVALID_CREDENTIALS, null));
		}
		
		String roleName = user.getRole().getRoleName();
		LoginResponseDto loginResponse = new LoginResponseDto();
		// generate token
		loginResponse.setAuthToken(jwtService.generateToken(email, roleName));
		loginResponse.setName(user.getName());
		loginResponse.setUserName(email);
		loginResponse.setRole(roleName);
		
		// return the response
		return ResponseEntity
				.status(HttpStatus.OK.value())
				.body(new ApiResponse<LoginResponseDto>(true, 200, "Login successful", loginResponse));
	}


	public boolean authenticate(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));
            return true; // Return true if authentication is successful
        } catch (Exception ex) {
            return false; // Return false if authentication fails
        }
    }

}
