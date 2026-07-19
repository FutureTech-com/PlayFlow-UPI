package com.payflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpendingAnalyticsResponse {
    private BigDecimal totalSpentThisMonth;
    private BigDecimal totalReceivedThisMonth;
    private List<MonthlyTotal> last6Months;
    private Map<String, BigDecimal> byType; // e.g. SEND, BILL_PAYMENT, SPLIT_BILL -> amount

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlyTotal {
        private String month; // "2026-07"
        private BigDecimal spent;
        private BigDecimal received;
    }
}
