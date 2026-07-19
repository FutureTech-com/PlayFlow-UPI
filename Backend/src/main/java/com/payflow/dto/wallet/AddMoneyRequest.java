package com.payflow.dto.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddMoneyRequest {

    @NotNull
    @DecimalMin(value = "1.00", message = "Minimum add-money amount is 1.00")
    private BigDecimal amount;

    @NotBlank
    private String source;          // e.g. UPI, DEBIT_CARD, NET_BANKING

    private String sourceReferenceId; // gateway/payment reference, if pre-authorized
}