package com.payflow.dto.bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillPaymentResponse {
    private String id;
    private String billerName;
    private String category;
    private String consumerIdentifier;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
}
