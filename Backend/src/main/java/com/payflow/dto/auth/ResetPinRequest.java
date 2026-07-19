package com.payflow.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPinRequest {

    @NotBlank
    private String otpReferenceId;   // ties this reset to a verified OTP session

    @NotBlank
    private String otp;

    @NotBlank
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must be 4-6 digits")
    private String newPin;
}