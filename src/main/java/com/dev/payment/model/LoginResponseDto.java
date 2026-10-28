package com.dev.payment.model;

import lombok.Data;

@Data
public class LoginResponseDto {
	
    private String name;
    private String userName;
    private String authToken;    
    private String role;

}
