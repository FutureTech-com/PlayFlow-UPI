package com.payflow.service;

import com.payflow.dto.user.SetTransactionPinRequest;
import com.payflow.dto.user.UpdateProfileRequest;
import com.payflow.dto.user.UserProfileResponse;
import com.payflow.entity.User;
import com.payflow.exception.BadRequestException;
import com.payflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuditLogService auditLogService;
	private final OtpService otpService;
	private final NotificationService notificationService;

	public UserProfileResponse getProfile(User user) {
		return UserProfileResponse.builder().id(user.getId()).fullName(user.getFullName()).email(user.getEmail())
				.phone(user.getPhone()).phoneVerified(user.isPhoneVerified()).emailVerified(user.isEmailVerified())
				.pinSet(user.isPinSet()).kycStatus(user.getKycStatus().name()).memberSince(user.getCreatedAt()).build();
	}

	@Transactional
	public UserProfileResponse updateProfile(User user, UpdateProfileRequest request) {
		user.setFullName(request.getFullName());
		userRepository.save(user);
		return getProfile(user);
	}

	@Transactional
	public void setTransactionPin(User user, SetTransactionPinRequest request) {
		user.setTransactionPinHash(passwordEncoder.encode(request.getPin()));
		user.setPinSet(true);
		userRepository.save(user);
		auditLogService.log(user.getId(), "TRANSACTION_PIN_SET", "User", user.getId(), null);
	}

	public void verifyTransactionPin(User user, String pin) {
		if (!user.isPinSet() || user.getTransactionPinHash() == null) {
			throw new BadRequestException("Transaction PIN is not set. Please set a PIN first.");
		}
		if (!passwordEncoder.matches(pin, user.getTransactionPinHash())) {
			throw new BadRequestException("Incorrect transaction PIN");
		}
	}

	/**
	 * Mobile number verification: sends an OTP to the user's registered phone
	 * (logged, not really SMS'd).
	 */
	@Transactional
	public void sendPhoneOtp(User user) {
		if (user.isPhoneVerified()) {
			throw new BadRequestException("Phone number is already verified");
		}
		otpService.generateOtp(user.getPhone(), com.payflow.entity.OtpVerification.OtpPurpose.PHONE_VERIFICATION);
	}

	@Transactional
	public void verifyPhoneOtp(User user, String otp) {
		otpService.verifyOtp(user.getPhone(), com.payflow.entity.OtpVerification.OtpPurpose.PHONE_VERIFICATION, otp);
		user.setPhoneVerified(true);
		userRepository.save(user);
		auditLogService.log(user.getId(), "PHONE_VERIFIED", "User", user.getId(), null);
		notificationService.createNotification(user, "Mobile number verified",
				"Your mobile number has been successfully verified.",
				com.payflow.entity.Notification.NotificationType.GENERAL);
	}

	/**
	 * UPI PIN reset via OTP - for when the user has forgotten their PIN (different
	 * flow from setTransactionPin, which requires being logged in but doesn't need
	 * the old PIN either; this variant additionally requires OTP proof of phone
	 * ownership).
	 */
	@Transactional
	public void sendPinResetOtp(User user) {
		otpService.generateOtp(user.getPhone(), com.payflow.entity.OtpVerification.OtpPurpose.PIN_RESET);
	}

	@Transactional
	public void resetPinWithOtp(User user, String otp, String newPin) {
		otpService.verifyOtp(user.getPhone(), com.payflow.entity.OtpVerification.OtpPurpose.PIN_RESET, otp);
		user.setTransactionPinHash(passwordEncoder.encode(newPin));
		user.setPinSet(true);
		userRepository.save(user);
		auditLogService.log(user.getId(), "TRANSACTION_PIN_RESET", "User", user.getId(), null);
		notificationService.createNotification(user, "Transaction PIN reset",
				"Your UPI transaction PIN was just reset. If this wasn't you, contact support immediately.",
				com.payflow.entity.Notification.NotificationType.SECURITY_ALERT);
	}
}
