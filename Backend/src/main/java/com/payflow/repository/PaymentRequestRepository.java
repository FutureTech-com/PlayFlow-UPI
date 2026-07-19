package com.payflow.repository;

import com.payflow.entity.PaymentRequest;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, String> {
    List<PaymentRequest> findByPayerOrderByCreatedAtDesc(User payer);
    List<PaymentRequest> findByRequesterOrderByCreatedAtDesc(User requester);
}
