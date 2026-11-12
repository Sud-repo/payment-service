package com.dev.payment.model;

import java.util.UUID;

import com.dev.payment.enums.MerchantStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponse {

    private UUID id;
    private String name;
    private String email;
    private MerchantStatus status;
    
}

