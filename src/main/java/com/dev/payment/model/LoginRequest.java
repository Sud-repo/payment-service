package com.dev.payment.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Email
    private String userName;

    @NotBlank(message = "Password is required")
    private String password;
    
}
