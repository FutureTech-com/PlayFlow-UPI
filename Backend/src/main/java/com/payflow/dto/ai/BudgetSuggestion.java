package com.payflow.dto.ai;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetSuggestion {
    private String category;
    private BigDecimal last3MonthAverage;
    private BigDecimal suggestedMonthlyBudget;
}