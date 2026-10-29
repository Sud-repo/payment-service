package com.dev.payment.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.payment.model.ApiResponse;
import com.dev.payment.model.LoginRequest;
import com.dev.payment.model.LoginResponseDto;
import com.dev.payment.security.service.LoginService;

@RestController
@RequestMapping(value = "/admin")
public class LoginController {

	private final LoginService loginService;

	public LoginController(LoginService loginService) {
		super();
		this.loginService = loginService;
	}
	
	
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponseDto>> login(
			@RequestHeader String email, @RequestHeader String password) {
		LoginRequest request = new LoginRequest(email, password);
		return loginService.authenticateUser(request);
	}
	
}
