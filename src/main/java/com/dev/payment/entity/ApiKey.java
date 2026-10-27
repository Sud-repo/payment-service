package com.dev.payment.entity;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "api_keys")
public class ApiKey {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "key_prefix")
	private String keyPrefix;
	
	@Column(name = "key_hash")
	private String keyHash;
	
	private Boolean status;
	
	@Column(name = "expires_at")
	private Date expiresAt;

	@ManyToOne
	@JoinColumn(name = "merchant_id")
	private Merchant merchant;

	@ElementCollection
	@CollectionTable(
		    name = "api_key_permissions",
		    joinColumns = @JoinColumn(name = "api_key_id")
		)
	private Set<String> permission;
		
}
