package com.dev.payment.entity;

import java.util.List;
import java.util.UUID;

import com.dev.payment.enums.MerchantStatus;
import com.dev.payment.security.entity.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table(name = "merchants")
public class Merchant {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Size(min = 3)
	private String name;

	@Enumerated(EnumType.STRING)
	private MerchantStatus status;

	@OneToMany(mappedBy = "merchant")
	@JsonManagedReference
	private List<ApiKey> apiKeys;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

}
