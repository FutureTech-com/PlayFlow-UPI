package com.payflow.dto.payment;

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
public class PaymentRequestResponse {
    private String id;
    private String requesterName;
    private String requesterVpa;
    private String payerName;
    private String payerVpa;
    private BigDecimal amount;
    private String note;
    private String status;
    private LocalDateTime createdAt;
	
}
