package com.payflow.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bank_account")
public class BankAccount extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "User_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String bankName;

	@Column(nullable = false)
	private String accountHolderName;

	@Column(nullable = false)
	private String accountNumber;

	@Column(nullable = false)
	private String ifscCode;

	@Column(nullable = false)
	private BigDecimal balance = new BigDecimal("10000.00");

	@Column(name = "primary_account")
	private boolean primaryAccount = false;

	private boolean verified = false;

	@Enumerated(EnumType.STRING)

	private AccountType accountType;

	/*
	 * @Enumerated(EnumType.STRING) private AccountType accountType =
	 * AccountType.SAVINGS;
	 */
	
	public enum AccountType {
		SAVINGS, CURRENT
	}

}
