package com.payflow.dto.bank;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkBankAccountRequest {
    @NotBlank
    private String bankName;

    @NotBlank
    private String accountHolderName;

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String ifscCode;

    private String accountType = "SAVINGS";

}
