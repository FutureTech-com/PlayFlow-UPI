package com.payflow.controller;

import com.payflow.dto.transaction.TransactionFilterRequest;
import com.payflow.dto.transaction.TransactionResponse;
import com.payflow.security.UserPrincipal;
import com.payflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "History, details, receipts")
public class TransactionController {

    private final TransactionService transactionService;
    
	@GetMapping
    @Operation(summary = "Get transaction history with optional filters")
    public ResponseEntity<List<TransactionResponse>> history(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String search) {

        TransactionFilterRequest filter = new TransactionFilterRequest();
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setStatus(status);
        filter.setType(type);
        filter.setMinAmount(minAmount);
        filter.setMaxAmount(maxAmount);
        filter.setSearch(search);

        return ResponseEntity.ok(transactionService.getHistory(principal.getUser(), filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get details of a single transaction")
    public ResponseEntity<TransactionResponse> details(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable String id) {
        return ResponseEntity.ok(transactionService.getDetails(principal.getUser(), id));
    }

    @SuppressWarnings("null")
	@GetMapping("/{id}/receipt")
    @Operation(summary = "Download a PDF receipt for a transaction")
    public ResponseEntity<byte[]> receipt(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable String id) throws IOException {
        byte[] pdf = transactionService.downloadReceipt(principal.getUser(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + id + ".pdf")
                .body(pdf);
    }
}
