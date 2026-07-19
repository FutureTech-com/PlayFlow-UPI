package com.payflow.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendMoneyRequest {
    @NotBlank(message = "Recipient VPA is required")
    private String receiverVpa;
    
    private String receiverMobile;
    private String receiverAccountNumber;
    private String receiverIfsc;
    
    @NotNull
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    private BigDecimal amount;

    private String note;

    @NotBlank(message = "Transaction PIN is required")
    private String transactionPin;

	
}
