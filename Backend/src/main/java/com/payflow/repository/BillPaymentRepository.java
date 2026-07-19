package com.payflow.repository;

import com.payflow.entity.BillPayment;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillPaymentRepository extends JpaRepository<BillPayment, String> {
    List<BillPayment> findByUserOrderByCreatedAtDesc(User user);
}
