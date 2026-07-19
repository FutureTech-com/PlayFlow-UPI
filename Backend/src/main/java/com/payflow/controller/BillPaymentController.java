package com.payflow.controller;

import com.payflow.dto.bill.BillPaymentResponse;
import com.payflow.dto.bill.BillerResponse;
import com.payflow.dto.bill.PayBillRequest;
import com.payflow.entity.Biller;
import com.payflow.security.UserPrincipal;
import com.payflow.service.BillPaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Recharge & Bills", description = "Mobile/DTH recharge, electricity/water/gas/broadband/FASTag/credit card bill payment")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    @GetMapping("/billers")
    @Operation(summary = "List billers, optionally filtered by category")
    public ResponseEntity<List<BillerResponse>> billers(@RequestParam(required = false) String category) {
        Biller.BillerCategory cat = category != null ? Biller.BillerCategory.valueOf(category.toUpperCase()) : null;
        return ResponseEntity.ok(billPaymentService.listBillers(cat));
    }

    @PostMapping("/pay")
    @Operation(summary = "Pay a bill / recharge (requires transaction PIN)")
    public ResponseEntity<BillPaymentResponse> pay(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody PayBillRequest request) {
        return ResponseEntity.ok(billPaymentService.pay(principal.getUser(), request));
    }

    @GetMapping("/history")
    @Operation(summary = "List your past bill payments")
    public ResponseEntity<List<BillPaymentResponse>> history(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(billPaymentService.history(principal.getUser()));
    }
}
