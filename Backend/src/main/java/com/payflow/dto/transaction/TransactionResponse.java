package com.payflow.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
	private String id;
	private String referenceId;
	private String type; // DEBIT / CREDIT from the viewer's perspective
	private String counterpartyName;
	private String counterpartyVpa;
	private BigDecimal amount;
	private String note;
	private String status;
	private LocalDateTime timestamp;
	private String category;
	private String riskLevel; // LOW / MEDIUM / HIGH, null for transaction types not scored (e.g.
								// self-transfer)

}
