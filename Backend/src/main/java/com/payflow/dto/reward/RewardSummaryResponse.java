package com.payflow.dto.reward;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardSummaryResponse {
    private BigDecimal totalCashback;
    private int totalPoints;
    private List<RewardEntry> history;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RewardEntry {
        private String id;
        private BigDecimal cashbackAmount;
        private int points;
        private String reason;
        private java.time.LocalDateTime createdAt;
    }
}
