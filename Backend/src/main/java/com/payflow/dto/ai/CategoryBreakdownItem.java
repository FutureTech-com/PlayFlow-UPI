package com.payflow.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryBreakdownItem {
    private String category;
    private BigDecimal totalSpent;
    private long transactionCount;
    private double percentOfTotal;
}
