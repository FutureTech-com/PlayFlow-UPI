package com.payflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "upi_id", uniqueConstraints = @UniqueConstraint(columnNames = "vpa"))
public class UpiId extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bank_account_id")
	private BankAccount bankAccount;

	/** Virtual Payment Address, e.g. rahul.sharma@payflow */
	@Column(nullable = false, unique = true)
	private String vpa;

	private boolean active = true;
	private boolean merchant = false;

	/** Base64-encoded PNG QR code representing this VPA. */
	
	@Lob
	@Column(columnDefinition = "TEXT")
	private String qrCodeBase64;

}
