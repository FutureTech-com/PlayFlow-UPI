package com.payflow.dto.splitbill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyShareResponse {
    private String shareId;
    private String billTitle;
    private String organizerName;
    private BigDecimal amountOwed;
    private boolean settled;
}