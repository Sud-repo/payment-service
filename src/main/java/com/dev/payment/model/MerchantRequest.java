package com.dev.payment.model;

import com.dev.payment.util.Constants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantRequest {

    @NotBlank(message = "User name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Merchant name is required")
    @Size(min = Constants.MERCHANT_PREFIX_LENGTH, 
    message = "Merchant name must be at least " +  Constants.MERCHANT_PREFIX_LENGTH + " characters")
    private String merchantName;

}

