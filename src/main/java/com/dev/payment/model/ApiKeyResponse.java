package com.dev.payment.model;

import java.util.Set;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiKeyResponse {

    private String apiKey;
    private Set<String> permissions;

    public ApiKeyResponse(String apiKey, Set<String> permissions) {
        this.apiKey = apiKey;
        this.permissions = permissions;
    }

}
