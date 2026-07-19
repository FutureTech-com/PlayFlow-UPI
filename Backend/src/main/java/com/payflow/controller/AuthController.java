package com.payflow.controller;

import com.payflow.dto.auth.*;
import com.payflow.security.UserPrincipal;
import com.payflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, password reset")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	@Operation(summary = "Register a new user")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/login")
	@Operation(summary = "Login with email and password")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest) {
		String userAgent = httpRequest.getHeader("User-Agent");
		String ip = httpRequest.getHeader("X-Forwarded-For") != null ? httpRequest.getHeader("X-Forwarded-For")
				: httpRequest.getRemoteAddr();
		return ResponseEntity.ok(authService.login(request, userAgent, ip));
	}

	@PostMapping("/login/mobile/send-otp")
	@Operation(summary = "Send a login OTP to a registered mobile number (passwordless login, step 1)")
	public ResponseEntity<Void> sendMobileLoginOtp(@Valid @RequestBody MobileLoginOtpRequest request) {
		authService.sendMobileLoginOtp(request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/login/mobile/verify-otp")
	@Operation(summary = "Verify the login OTP and complete passwordless login (step 2)")
	public ResponseEntity<AuthResponse> loginWithMobileOtp(@Valid @RequestBody MobileLoginVerifyRequest request,
			HttpServletRequest httpRequest) {
		String userAgent = httpRequest.getHeader("User-Agent");
		String ip = httpRequest.getHeader("X-Forwarded-For") != null ? httpRequest.getHeader("X-Forwarded-For")
				: httpRequest.getRemoteAddr();
		return ResponseEntity.ok(authService.loginWithMobileOtp(request, userAgent, ip));
	}

	@PostMapping("/refresh")
	@Operation(summary = "Exchange a refresh token for a new access token")
	public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ResponseEntity.ok(authService.refresh(request));
	}

	@PostMapping("/forgot-password")
	@Operation(summary = "Request an OTP to reset password")
	public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/reset-password")
	@Operation(summary = "Reset password using OTP")
	public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/change-password")
	@Operation(summary = "Change password while logged in")
	public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody ChangePasswordRequest request) {
		authService.changePassword(principal.getUser(), request);
		return ResponseEntity.noContent().build();
	}
}