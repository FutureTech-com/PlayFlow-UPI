package com.payflow.dto.bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FetchedAccountPreview {
    /** Index into the deterministic result set for this mobile number - pass back to /link-by-mobile to confirm. */
    private int accountIndex;
    private String bankName;
    private String accountHolderName;
    private String maskedAccountNumber;
    private String ifscCode;
    private String accountType;
}