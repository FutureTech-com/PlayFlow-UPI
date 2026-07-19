package com.payflow.dto.wallet;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class WalletResponse {
    private String id;
    private String userId;
    private BigDecimal balance;
    private BigDecimal rewardBalance;
    private String currency;
    private boolean frozen;
    private Instant updatedAt;
}