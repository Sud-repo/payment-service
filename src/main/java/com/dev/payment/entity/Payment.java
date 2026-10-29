package com.dev.payment.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import com.dev.payment.enums.PaymentStatus;

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
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	private BigDecimal amount;
	
	private String currency;
	
	@Enumerated(EnumType.STRING)
	private PaymentStatus status;
	
	@Column(name = "created_at")
	private Date createdAt;
	
	@Column(name = "order_id")
	private String orderId;

	@ManyToOne
	@JoinColumn(name = "merchant_id")
	private Merchant merchant;

}
