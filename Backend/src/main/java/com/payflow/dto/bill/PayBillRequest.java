package com.payflow.dto.bill;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayBillRequest {
    @NotBlank
    private String billerId;

    @NotBlank(message = "Consumer/account/mobile number is required")
    private String consumerIdentifier;

    @NotNull
    @DecimalMin(value = "1.00")
    private BigDecimal amount;

    @NotBlank(message = "Transaction PIN is required")
    private String transactionPin;
}
