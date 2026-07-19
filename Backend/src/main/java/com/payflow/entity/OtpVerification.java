package com.payflow.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "otp_verifications")
public class OtpVerification extends BaseEntity {

	@Column(nullable = false)
	private String identifier;

	@Column(nullable = false)
	private String otpHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OtpPurpose purpose;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	private boolean used = false;

	private int attempts = 0;

	public enum OtpPurpose {
		FORGOT_PASSWORD, PHONE_VERIFICATION, EMAIL_VERIFICATION, TRANSACTION_CONFIRM, PIN_RESET, LOGIN_OTP
	}

}
