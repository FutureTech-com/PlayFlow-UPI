package com.payflow.dto.ai;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpendingInsightsResponse {
    /** Natural-language observations, e.g. "You spent 32% more on Food & Dining this month." */
    private List<String> insights;
    private BigDecimal currentMonthTotal;
    private BigDecimal previousMonthTotal;
    private List<CategoryBreakdownItem> categoryBreakdown;
}
