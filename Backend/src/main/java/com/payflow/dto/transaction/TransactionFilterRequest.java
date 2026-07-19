package com.payflow.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFilterRequest {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;     // SUCCESS, FAILED, PENDING
    private String type;       // SEND, RECEIVE, REQUEST, WALLET_ADD, WALLET_TO_BANK
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String search;     // matches counterparty name/vpa/reference id
	
}
