package com.payflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.payflow.dto.wallet.AddMoneyRequest;
import com.payflow.dto.wallet.WalletResponse;
import com.payflow.dto.wallet.WalletToBankRequest;
import com.payflow.security.UserPrincipal;
import com.payflow.service.WalletService;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "In-app wallet: add money, check balance, transfer to bank")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    @Operation(summary = "Get wallet balance")
    public ResponseEntity<WalletResponse> get(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(walletService.getWallet(principal.getUser()));
    }

    @PostMapping("/add")
    @Operation(summary = "Add money to wallet (simulated top-up)")
    public ResponseEntity<WalletResponse> addMoney(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody AddMoneyRequest request) {
        return ResponseEntity.ok(walletService.addMoney(principal.getUser(), request));
    }

    @PostMapping("/to-bank")
    @Operation(summary = "Transfer wallet balance to your primary bank account")
    public ResponseEntity<WalletResponse> toBank(@AuthenticationPrincipal UserPrincipal principal,
                                                   @Valid @RequestBody WalletToBankRequest request) {
        return ResponseEntity.ok(walletService.transferToBank(principal.getUser(), request));
    }
}