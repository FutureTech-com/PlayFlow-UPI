package com.payflow.controller;

import com.payflow.dto.payment.*;
import com.payflow.dto.transaction.TransactionResponse;
import com.payflow.dto.transfer.SelfTransferRequest;
import com.payflow.security.UserPrincipal;
import com.payflow.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Send money, request money")
public class PaymentController {

    private final PaymentService paymentService;
    
    @PostMapping("/send")
    @Operation(summary = "Send money to a UPI ID (requires transaction PIN)")
    public ResponseEntity<TransactionResponse> send(@AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody SendMoneyRequest request) {
        return ResponseEntity.ok(paymentService.sendMoney(principal.getUser(), request));
    }

    @PostMapping("/self-transfer")
    @Operation(summary = "Move money between two of your own linked bank accounts")
    public ResponseEntity<TransactionResponse> selfTransfer(@AuthenticationPrincipal UserPrincipal principal,
                                                              @Valid @RequestBody SelfTransferRequest request) {
        return ResponseEntity.ok(paymentService.selfTransfer(principal.getUser(), request));
    }
    
    @PostMapping("/request")
    @Operation(summary = "Request money from another UPI ID")
    public ResponseEntity<PaymentRequestResponse> request(@AuthenticationPrincipal UserPrincipal principal,
                                                            @Valid @RequestBody RequestMoneyRequest request) {
        return ResponseEntity.ok(paymentService.requestMoney(principal.getUser(), request));
    }

    @PostMapping("/requests/{id}/respond")
    @Operation(summary = "Accept or decline an incoming payment request")
    public ResponseEntity<PaymentRequestResponse> respond(@AuthenticationPrincipal UserPrincipal principal,
                                                            @PathVariable String id,
                                                            @Valid @RequestBody RespondToRequestRequest request) {
        return ResponseEntity.ok(paymentService.respondToRequest(principal.getUser(), id, request));
    }

    @GetMapping("/requests/incoming")
    @Operation(summary = "List payment requests addressed to you")
    public ResponseEntity<List<PaymentRequestResponse>> incoming(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(paymentService.listIncomingRequests(principal.getUser()));
    }

    @GetMapping("/requests/outgoing")
    @Operation(summary = "List payment requests you have sent")
    public ResponseEntity<List<PaymentRequestResponse>> outgoing(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(paymentService.listOutgoingRequests(principal.getUser()));
    }
}
