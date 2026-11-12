package com.dev.payment.security.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.payment.security.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
	
	Optional<Role> findByRoleName(String roleName);

}
