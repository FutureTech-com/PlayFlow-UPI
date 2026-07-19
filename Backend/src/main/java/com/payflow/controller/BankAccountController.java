package com.payflow.controller;

import com.payflow.dto.bank.BankAccountResponse;
import com.payflow.dto.bank.FetchAccountsByMobileRequest;
import com.payflow.dto.bank.FetchedAccountPreview;
import com.payflow.dto.bank.LinkBankAccountRequest;
import com.payflow.dto.bank.LinkByMobileRequest;
import com.payflow.security.UserPrincipal;
import com.payflow.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
@Tag(name = "Bank Accounts", description = "Link and manage bank accounts")
public class BankAccountController {

	private final BankAccountService bankAccountService;

	@PostMapping("/link")
	@Operation(summary = "Link a new bank account (simulated instant verification)")
	public ResponseEntity<BankAccountResponse> link(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody LinkBankAccountRequest request) {
		return ResponseEntity.ok(bankAccountService.linkAccount(principal.getUser(), request));
	}

	@PostMapping("/fetch-by-mobile")
	@Operation(summary = "Discover bank account(s) linked to a mobile number (simulated NPCI-style lookup), for review before linking")
	public ResponseEntity<List<FetchedAccountPreview>> fetchByMobile(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody FetchAccountsByMobileRequest request) {
		return ResponseEntity
				.ok(bankAccountService.fetchAccountsForMobile(principal.getUser(), request.getMobileNumber()));
	}

	@PostMapping("/link-by-mobile")
	@Operation(summary = "Link one of the accounts previously returned by /fetch-by-mobile")
	public ResponseEntity<BankAccountResponse> linkByMobile(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody LinkByMobileRequest request) {
		return ResponseEntity.ok(bankAccountService.linkAccountByMobile(principal.getUser(), request));
	}

	@GetMapping("/accounts")
	@Operation(summary = "List all bank accounts linked to the current user")
	public ResponseEntity<List<BankAccountResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(bankAccountService.listAccounts(principal.getUser()));
	}

	@PatchMapping("/accounts/{id}/primary")
	@Operation(summary = "Set a bank account as the primary account for payments")
	public ResponseEntity<BankAccountResponse> setPrimary(@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable String id) {
		return ResponseEntity.ok(bankAccountService.setPrimary(principal.getUser(), id));
	}
}
