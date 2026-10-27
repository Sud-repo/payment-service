package com.dev.payment.entity;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "idempotency_records")
public class IdempotencyRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "idempotency_key")
	private String idempotencyKey;

	@Column(name = "request_hash")
	private String requestHash;

	private String status;
	
	@Column(name = "expires_at")
	private Date expiresAt;

	@ManyToOne
	@JoinColumn(name = "merchant_id")
	private Merchant merchant;
	
	@OneToOne
	@JoinColumn(name = "payment_id")
	private Payment payment;
	
}
