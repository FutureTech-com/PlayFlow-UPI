package com.payflow.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudAlertResponse {
    private String transactionId;
    private String referenceId;
    private String userFullName; // populated only for the admin-facing endpoint
    private BigDecimal amount;
    private String counterpartyVpa;
    private int riskScore;
    private String riskLevel;
    private List<String> reasons;
    private LocalDateTime timestamp;
}
