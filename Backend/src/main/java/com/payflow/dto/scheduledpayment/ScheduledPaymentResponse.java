package com.payflow.dto.scheduledpayment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ScheduledPaymentResponse {
    private String id;
    private String receiverVpa;
    private BigDecimal amount;
    private String note;
    private String recurrence;
    private LocalDateTime nextRunAt;
    private LocalDateTime lastRunAt;
    private boolean active;
    private String lastFailureReason;
}