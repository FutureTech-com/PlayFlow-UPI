package com.payflow.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyPhoneOtpRequest {
	 @NotBlank
	    private String phoneNumber;

	    @NotBlank
	    @Pattern(regexp = "^\\d{4,6}$", message = "OTP must be 4-6 digits")
	    private String otp;

	    @NotBlank
	    private String otpReferenceId;
}
