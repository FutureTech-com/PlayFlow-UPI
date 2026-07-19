package com.payflow.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SelfTransferRequest {
    @NotBlank(message = "Source bank account is required")
    private String fromBankAccountId;

    @NotBlank(message = "Destination bank account is required")
    private String toBankAccountId;

    @NotNull
    @DecimalMin(value = "1.00")
    private BigDecimal amount;

    @NotBlank(message = "Transaction PIN is required")
    private String transactionPin;
}
