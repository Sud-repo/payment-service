package com.dev.payment.security.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dev.payment.entity.Merchant;
import com.dev.payment.exception.AuthenticationException;
import com.dev.payment.exception.ResourceNotFoundException;
import com.dev.payment.model.ApiResponse;
import com.dev.payment.model.LoginRequest;
import com.dev.payment.model.LoginResponseDto;
import com.dev.payment.model.MerchantRequest;
import com.dev.payment.repository.MerchantRepository;
import com.dev.payment.security.entity.Role;
import com.dev.payment.security.entity.User;
import com.dev.payment.security.repository.RoleRepository;
import com.dev.payment.security.repository.UserRepository;
import com.dev.payment.util.Constants;

@Service
public class LoginService {

	
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;	
	private final MerchantRepository merchantRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	
	
	public LoginService(JwtService jwtService, UserRepository userRepository, 
			RoleRepository roleRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, MerchantRepository merchantRepository) {
		super();
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.merchantRepository = merchantRepository;
	}


	public ResponseEntity<ApiResponse<LoginResponseDto>> authenticateUser(LoginRequest loginRequest) {
		String email = loginRequest.getUserName();
		
		// check whether the user exists
		Optional<User> optUser = userRepository.findByUserName(email);
		// authenticate the user
		if (optUser.isEmpty() || !authenticate(loginRequest)) {
			throw new AuthenticationException(Constants.INVALID_CREDENTIALS);
		}
		
		User user = optUser.get();
		if (!Boolean.TRUE.equals(user.getIsActive())) {
			throw new AuthenticationException(Constants.INVALID_CREDENTIALS);
		}
		
		String roleName = user.getRole().getRoleName();
		Merchant merchant = merchantRepository.findByUser_Id(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Merchant Not Fount"));
		LoginResponseDto loginResponse = new LoginResponseDto();
		// generate token
		loginResponse.setAuthToken(jwtService.generateToken(email, roleName, merchant.getName()));
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


	public boolean existsByEmail(String userName) {
		return userRepository.existsByUserName(userName);
	}


	public User createMerchantUser(MerchantRequest merchantRequest) {
		Role merchantRole = roleRepository.findByRoleName(Constants.MERCHANT_ROLE)
				.orElseThrow(() -> new ResourceNotFoundException("Role Not Found"));
			
		User user = new User();
		user.setName(merchantRequest.getName());
		user.setUserName(merchantRequest.getEmail());
		user.setPassword(passwordEncoder.encode(merchantRequest.getPassword()));
		user.setIsActive(false);
		user.setRole(merchantRole);
		
		return userRepository.save(user);
	}


	public void updateUserStatus(User user, boolean isActive) {
		user.setIsActive(isActive);
		userRepository.save(user);
	}

}
