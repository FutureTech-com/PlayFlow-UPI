package com.payflow.controller;

import com.payflow.dto.scheduledpayment.CreateScheduledPaymentRequest;
import com.payflow.dto.scheduledpayment.ScheduledPaymentResponse;
import com.payflow.security.UserPrincipal;
import com.payflow.service.ScheduledPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-payments")
@RequiredArgsConstructor
@Tag(name = "Scheduled Payments", description = "One-time scheduled payments and recurring AutoPay mandates")
public class ScheduledPaymentController {

    private final ScheduledPaymentService scheduledPaymentService;

    @PostMapping
    @Operation(summary = "Create a scheduled or recurring (AutoPay) payment")
    public ResponseEntity<ScheduledPaymentResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                             @Valid @RequestBody CreateScheduledPaymentRequest request) {
        return ResponseEntity.ok(scheduledPaymentService.create(principal.getUser(), request));
    }

    @GetMapping
    @Operation(summary = "List your scheduled/recurring payments")
    public ResponseEntity<List<ScheduledPaymentResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(scheduledPaymentService.list(principal.getUser()));
    }

    @SuppressWarnings("null")
	@DeleteMapping("/{id}")
    @Operation(summary = "Cancel a scheduled/recurring payment")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        scheduledPaymentService.cancel(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
