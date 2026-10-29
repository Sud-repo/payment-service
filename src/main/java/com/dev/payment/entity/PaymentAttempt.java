package com.dev.payment.entity;

import java.util.Date;
import java.util.UUID;

import com.dev.payment.enums.PaymentAttemptStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "payment_attempts")
public class PaymentAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	private Integer attemptNumber;
	
	private String paymentProvider;
	
	private String providerPaymentId;

	@Enumerated(EnumType.STRING)
	private PaymentAttemptStatus status;
	
	private String comments;

	@Column(name = "created_at")
	private Date createdAt;
	
	@Column(name = "completed_at")
	private Date completedAt;
	
	@ManyToOne
	@JoinColumn(name = "payment_id")
	private Payment payment;


}
