package com.dev.payment.model;

import java.util.Set;

import lombok.Data;

@Data
public class ApiKeyResponse {

    private String apiKey;
    private Set<String> permissions;

}
