// dto/scheduledpayment/CreateScheduledPaymentRequest.java
package com.payflow.dto.scheduledpayment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateScheduledPaymentRequest {

    @NotBlank
    private String receiverVpa;

    @NotNull
    @DecimalMin(value = "1.00")
    private BigDecimal amount;

    private String note;

    @NotBlank
    private String recurrence;   // ONCE, DAILY, WEEKLY, MONTHLY - validated/parsed in the service

    @NotNull
    @FutureOrPresent
    private LocalDateTime startAt;
}