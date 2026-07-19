package com.payflow.repository;

import com.payflow.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, String> {
    Optional<OtpVerification> findTopByIdentifierAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String identifier, OtpVerification.OtpPurpose purpose);
}
