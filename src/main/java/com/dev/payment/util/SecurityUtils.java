package com.dev.payment.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dev.payment.model.MerchantContext;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static MerchantContext getCurrentMerchant() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof MerchantContext)) {
            throw new IllegalStateException(
                    "No authenticated merchant found"
            );
        }

        return (MerchantContext) authentication.getPrincipal();
    }
}
