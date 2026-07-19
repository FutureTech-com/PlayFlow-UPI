package com.payflow.controller;

import com.payflow.dto.auth.ResetPinRequest;
import com.payflow.dto.auth.VerifyPhoneOtpRequest;
import com.payflow.dto.user.SetTransactionPinRequest;
import com.payflow.dto.user.UpdateProfileRequest;
import com.payflow.dto.user.UserProfileResponse;
import com.payflow.security.UserPrincipal;
import com.payflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "Profile management")
public class UserController {

	private final UserService userService;

	@GetMapping("/profile")
	@Operation(summary = "Get current user's profile")
	public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseEntity.ok(userService.getProfile(principal.getUser()));
	}

	@PutMapping("/profile")
	@Operation(summary = "Update current user's profile")
	public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody UpdateProfileRequest request) {
		return ResponseEntity.ok(userService.updateProfile(principal.getUser(), request));
	}

	@PostMapping("/transaction-pin")
	@Operation(summary = "Set or update the 4-6 digit UPI transaction PIN")
	public ResponseEntity<Void> setTransactionPin(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody SetTransactionPinRequest request) {
		userService.setTransactionPin(principal.getUser(), request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/phone/send-otp")
	@Operation(summary = "Send an OTP to verify your registered mobile number")
	public ResponseEntity<Void> sendPhoneOtp(@AuthenticationPrincipal UserPrincipal principal) {
		userService.sendPhoneOtp(principal.getUser());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/phone/verify-otp")
	@Operation(summary = "Verify your mobile number with the OTP you received")
	public ResponseEntity<Void> verifyPhoneOtp(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody VerifyPhoneOtpRequest request) {
		userService.verifyPhoneOtp(principal.getUser(), request.getOtp());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/transaction-pin/forgot")
	@Operation(summary = "Send an OTP to your phone to authorize a UPI PIN reset")
	public ResponseEntity<Void> forgotPin(@AuthenticationPrincipal UserPrincipal principal) {
		userService.sendPinResetOtp(principal.getUser());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/transaction-pin/reset")
	@Operation(summary = "Reset your UPI transaction PIN using the OTP sent to your phone")
	public ResponseEntity<Void> resetPin(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody ResetPinRequest request) {
		userService.resetPinWithOtp(principal.getUser(), request.getOtp(), request.getNewPin());
		return ResponseEntity.noContent().build();
	}
}
