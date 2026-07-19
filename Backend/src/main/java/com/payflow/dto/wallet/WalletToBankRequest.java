package com.payflow.dto.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletToBankRequest {

    @NotNull
    @DecimalMin(value = "1.00", message = "Minimum withdrawal amount is 1.00")
    private BigDecimal amount;

    @NotBlank
    private String bankAccountId;   // reference to a saved/linked bank account

    private String remarks;
}