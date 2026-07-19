package com.payflow.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "transactions", indexes = { @Index(name = "idx_txn_sender", columnList = "sender_id"),
		@Index(name = "idx_txn_receiver", columnList = "receiver_id"),
		@Index(name = "idx_txn_ref", columnList = "referenceId", unique = true) })
public class Transaction extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String referenceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id")
	private User sender;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id")
	private User receiver;

	private String senderVpa;
	private String receiverVpa;

	@Column(nullable = false)
	private BigDecimal amount;

	private String note;

	@Column(nullable = false)
	private BigDecimal accountBalance;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionType transactionType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionStatus status;

	private String failureReason;

	/**
	 * Loose classification for analytics/expense tracking, e.g. FOOD, BILLS,
	 * SHOPPING, TRANSFER. Nullable.
	 */
	private String category;

	/**
	 * Set by FraudDetectionService.assess() at send-time. Null for transaction
	 * types it doesn't score (e.g. self-transfer).
	 */
	private Integer riskScore;

	@Enumerated(EnumType.STRING)
	private RiskLevel riskLevel;

	/**
	 * Pipe-separated human-readable reasons from FraudDetectionService, shown in
	 * the fraud alert views.
	 */
	@Column(length = 1000)
	private String riskReasons;
	/**
	 * True when riskLevel is HIGH - surfaced in the admin "Fraud Monitoring" view.
	 */
	private boolean flaggedForReview = false;

	public enum RiskLevel {
		LOW, MEDIUM, HIGH
	}

	/**
	 * Loose classification for analytics/expense tracking, e.g. FOOD, BILLS,
	 * SHOPPING, TRANSFER. Nullable.
	 */

	public enum TransactionType {
		SEND, RECEIVE, REQUEST, WALLET_ADD, WALLET_TO_BANK, SELF_TRANSFER, 
		SPLIT_BILL, BILL_PAYMENT, SCHEDULED,
		MERCHANT_PAYMENT, CASHBACK
	}

	public enum TransactionStatus {
		PENDING, SUCCESS, FAILED, CANCELLED
	}

}
