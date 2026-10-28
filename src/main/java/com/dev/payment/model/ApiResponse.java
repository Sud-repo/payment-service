package com.dev.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;    
    private Integer statusCode;
    private String message;
    private T data;
    
}