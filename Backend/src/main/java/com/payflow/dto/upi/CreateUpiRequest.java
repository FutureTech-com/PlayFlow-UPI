package com.payflow.dto.upi;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUpiRequest {
    @NotBlank(message = "Bank account is required")
    private String bankAccountId;

    /** Optional custom prefix, e.g. "rahul.sharma" -> rahul.sharma@payflow */
    private String preferredHandle;
    

    /** Marks this VPA as a merchant/business identity (enables dynamic QR with amount). */
    private boolean merchant = false;

    
}
