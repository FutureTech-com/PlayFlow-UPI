package com.payflow.controller;

import com.payflow.dto.upi.CreateUpiRequest;
import com.payflow.dto.upi.UpiIdResponse;
import com.payflow.security.UserPrincipal;
import com.payflow.service.UpiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/upi")
@RequiredArgsConstructor
@Tag(name = "UPI", description = "UPI ID creation and QR codes")
public class UpiController {

    private final UpiService upiService;
    
	@PostMapping("/create")
    @Operation(summary = "Create a UPI ID (VPA) linked to a bank account, with a QR code")
    public ResponseEntity<UpiIdResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                 @Valid @RequestBody CreateUpiRequest request) {
        return ResponseEntity.ok(upiService.createUpiId(principal.getUser(), request));
    }

    @GetMapping
    @Operation(summary = "List UPI IDs belonging to the current user")
    public ResponseEntity<List<UpiIdResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(upiService.listUpiIds(principal.getUser()));
    }

    @GetMapping("/details")
    @Operation(summary = "Look up a UPI ID's public details (e.g. before sending money / after scanning a QR)")
    public ResponseEntity<UpiIdResponse> details(@AuthenticationPrincipal UserPrincipal principal,
                                                  @RequestParam String vpa) {
        return ResponseEntity.ok(upiService.getDetails(principal.getUser(), vpa));
    }
    
    @GetMapping("/qr/dynamic")
    @Operation(summary = "Generate a dynamic QR (own VPA) that encodes a fixed amount and note - for merchant-style 'charge this much' codes")
    public ResponseEntity<java.util.Map<String, String>> dynamicQr(@AuthenticationPrincipal UserPrincipal principal,
                                                                     @RequestParam String vpa,
                                                                     @RequestParam(required = false) java.math.BigDecimal amount,
                                                                     @RequestParam(required = false) String note) {
        String qr = upiService.generateDynamicQr(principal.getUser(), vpa, amount, note);
        return ResponseEntity.ok(java.util.Map.of("qrCodeBase64", qr));
    }
}
