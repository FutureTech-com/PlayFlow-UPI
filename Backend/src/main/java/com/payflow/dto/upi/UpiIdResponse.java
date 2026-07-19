package com.payflow.dto.upi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpiIdResponse {
    private String id;
    private String vpa;
    private boolean active;
    private boolean merchant;
    private String linkedBankAccountId;
    private String linkedBankName;
    private String qrCodeBase64;
	
}
