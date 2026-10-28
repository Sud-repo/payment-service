package com.dev.payment.security.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.payment.security.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByUserName(String userName);

}
