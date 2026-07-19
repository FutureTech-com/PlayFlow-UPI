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
public class BudgetSuggestionsResponse {
    private List<BudgetSuggestion> suggestions;
    private BigDecimal suggestedOverallMonthlyBudget;
    private String basis; // short explanation of the method used, shown in the UI for transparency
}