package com.payflow.service;

import com.payflow.dto.auth.*;
import com.payflow.entity.OtpVerification;
import com.payflow.entity.Role;
import com.payflow.entity.User;
import com.payflow.exception.BadRequestException;
import com.payflow.exception.UnauthorizedException;
import com.payflow.repository.UserRepository;
import com.payflow.security.JwtTokenProvider;
import com.payflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final OtpService otpService;
	private final AuditLogService auditLogService;
	private final TrustedDeviceService trustedDeviceService;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BadRequestException("An account with this email already exists");
		}
		if (userRepository.existsByPhone(request.getPhone())) {
			throw new BadRequestException("An account with this phone number already exists");
		}

		User user = new User();
		user.setFullName(request.getFullName());
		user.setEmail(request.getEmail().toLowerCase());
		user.setPhone(request.getPhone());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setRoles(Set.of(Role.ROLE_USER));

		User saved = userRepository.save(user);
		auditLogService.log(saved.getId(), "USER_REGISTERED", "User", saved.getId(), "New account created");

		UserPrincipal principal = new UserPrincipal(saved);
		return buildAuthResponse(principal, saved);
	}

	public AuthResponse login(LoginRequest request, String userAgent, String ipAddress) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword()));

			UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
			auditLogService.log(principal.getId(), "LOGIN_SUCCESS", "User", principal.getId(), null);
			trustedDeviceService.recordLogin(principal.getUser(), userAgent, ipAddress);
			return buildAuthResponse(principal, principal.getUser());
		} catch (BadCredentialsException ex) {
			auditLogService.log(null, "LOGIN_FAILED", "User", null, "email=" + request.getEmail());
			throw ex;
		}
	}

	public AuthResponse refresh(RefreshTokenRequest request) {
		String token = request.getRefreshToken();
		if (!jwtTokenProvider.validateToken(token)) {
			throw new UnauthorizedException("Invalid or expired refresh token");
		}
		String email = jwtTokenProvider.getEmailFromToken(token);
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UnauthorizedException("User no longer exists"));

		UserPrincipal principal = new UserPrincipal(user);
		return buildAuthResponse(principal, user);
	}

	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {
		User user = userRepository.findByEmail(request.getEmail().toLowerCase())
				.orElseThrow(() -> new BadRequestException("No account found with this email"));
		otpService.generateOtp(user.getEmail(), OtpVerification.OtpPurpose.FORGOT_PASSWORD);
	}

	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		User user = userRepository.findByEmail(request.getEmail().toLowerCase())
				.orElseThrow(() -> new BadRequestException("No account found with this email"));

		otpService.verifyOtp(user.getEmail(), OtpVerification.OtpPurpose.FORGOT_PASSWORD, request.getOtp());

		user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
		auditLogService.log(user.getId(), "PASSWORD_RESET", "User", user.getId(), null);
	}

	@Transactional
	public void changePassword(User user, ChangePasswordRequest request) {
		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
			throw new BadRequestException("Current password is incorrect");
		}
		user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
		auditLogService.log(user.getId(), "PASSWORD_CHANGED", "User", user.getId(), null);
	}

	@Transactional
	public void sendMobileLoginOtp(MobileLoginOtpRequest request) {
		User user = userRepository.findByPhone(request.getPhone())
				.orElseThrow(() -> new BadRequestException("No account found with this mobile number"));
		if (!user.isEnabled()) {
			throw new BadRequestException("This account has been disabled. Contact support.");
		}
		otpService.generateOtp(user.getPhone(), OtpVerification.OtpPurpose.LOGIN_OTP);
	}

	@Transactional
	public AuthResponse loginWithMobileOtp(MobileLoginVerifyRequest request, String userAgent, String ipAddress) {
		User user = userRepository.findByPhone(request.getPhone())
				.orElseThrow(() -> new BadRequestException("No account found with this mobile number"));

		try {
			otpService.verifyOtp(user.getPhone(), OtpVerification.OtpPurpose.LOGIN_OTP, request.getOtp());
		} catch (BadRequestException ex) {
			auditLogService.log(user.getId(), "LOGIN_FAILED", "User", user.getId(),
					"mobile OTP login: " + ex.getMessage());
			throw ex;
		}

		UserPrincipal principal = new UserPrincipal(user);
		auditLogService.log(user.getId(), "LOGIN_SUCCESS", "User", user.getId(), "via mobile OTP");
		trustedDeviceService.recordLogin(user, userAgent, ipAddress);
		return buildAuthResponse(principal, user);
	}

	private AuthResponse buildAuthResponse(UserPrincipal principal, User user) {
		String accessToken = jwtTokenProvider.generateAccessToken(principal);
		String refreshToken = jwtTokenProvider.generateRefreshToken(principal);

		return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType("Bearer")
				.user(AuthResponse.UserSummary.builder().id(user.getId()).fullName(user.getFullName())
						.email(user.getEmail()).phone(user.getPhone()).build())
				.build();
	}
}
