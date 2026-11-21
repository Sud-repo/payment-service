package com.dev.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.payment.entity.ApiKey;
import com.dev.payment.entity.Merchant;
import com.dev.payment.enums.Environment;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

	Optional<ApiKey> findByKeyPrefixAndIsActiveTrue(String keyPrefix);

	int countByMerchant(Merchant merchant);

	boolean existsByMerchantIdAndEnvironment(UUID merchantId, Environment environment);

}
