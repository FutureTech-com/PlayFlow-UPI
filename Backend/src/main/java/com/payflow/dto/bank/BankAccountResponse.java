package com.payflow.dto.bank;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BankAccountResponse {
    private String id;
    private String bankName;
    private String accountHolderName;
    private String maskedAccountNumber;
    private String ifscCode;
    private BigDecimal balance;
    private boolean primary;
    private boolean verified;
    private String accountType;
}
