package com.dev.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.payment.entity.ApiKey;
import com.dev.payment.entity.Merchant;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

	Optional<ApiKey> findByKeyPrefixAndIsActiveTrue(String keyPrefix);

	int countByMerchant(Merchant merchant);

}
