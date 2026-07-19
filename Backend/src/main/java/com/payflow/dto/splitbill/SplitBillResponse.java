package com.payflow.dto.splitbill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SplitBillResponse {
    private String id;
    private String title;
    private String description;
    private BigDecimal totalAmount;
    private String organizerName;
    private String status;
    private List<ShareView> shares;
    private Instant createdAt;
    private Instant settledAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShareView {
        private String id;
        private String participantName;
        private String participantVpa;
        private BigDecimal amountOwed;
        private boolean settled;
        private Instant settledAt;
    }
}