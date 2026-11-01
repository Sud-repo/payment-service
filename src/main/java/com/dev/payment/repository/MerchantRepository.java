package com.dev.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dev.payment.entity.Merchant;

import jakarta.persistence.LockModeType;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

	boolean existsByName(String name);

	boolean existsByEmail(String email);

	boolean existsByNameOrEmail(String name, String email);

	Optional<Merchant> findByName(String name);

	Optional<Merchant> findByEmail(String email);

	Optional<Merchant> findByNameOrEmail(String name, String email);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM Merchant m WHERE m.id = :id")
	Optional<Merchant> findByIdForUpdate(@Param("id") UUID id);

}
