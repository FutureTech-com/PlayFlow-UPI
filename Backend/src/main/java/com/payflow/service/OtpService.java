package com.payflow.service;

import com.payflow.entity.OtpVerification;
import com.payflow.exception.BadRequestException;
import com.payflow.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final BCryptPasswordEncoder otpEncoder = new BCryptPasswordEncoder();

	@Value("${payflow.otp.expiry-seconds:300}")
    private long expirySeconds;

    @Value("${payflow.otp.length:6}")
    private int otpLength;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOtp(String identifier, OtpVerification.OtpPurpose purpose) {
        String otp = randomNumericString(otpLength);

        OtpVerification record = new OtpVerification();
        record.setIdentifier(identifier);
        record.setOtpHash(otpEncoder.encode(otp));
        record.setPurpose(purpose);
        record.setExpiresAt(LocalDateTime.now().plusSeconds(expirySeconds));
        otpVerificationRepository.save(record);

        // DEMO ONLY: in a real system this would be dispatched via SMS/email provider.
        log.info("[DEMO OTP] identifier={} purpose={} otp={}", identifier, purpose, otp);

        return otp;
    }

    public void verifyOtp(String identifier, OtpVerification.OtpPurpose purpose, String suppliedOtp) {
        OtpVerification record = otpVerificationRepository
                .findTopByIdentifierAndPurposeAndUsedFalseOrderByCreatedAtDesc(identifier, purpose)
                .orElseThrow(() -> new BadRequestException("No active OTP found. Please request a new one."));

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }
        if (record.getAttempts() >= 5) {
            throw new BadRequestException("Too many incorrect attempts. Please request a new OTP.");
        }
        if (!otpEncoder.matches(suppliedOtp, record.getOtpHash())) {
            record.setAttempts(record.getAttempts() + 1);
            otpVerificationRepository.save(record);
            throw new BadRequestException("Incorrect OTP.");
        }

        record.setUsed(true);
        otpVerificationRepository.save(record);
    }

    private String randomNumericString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}
