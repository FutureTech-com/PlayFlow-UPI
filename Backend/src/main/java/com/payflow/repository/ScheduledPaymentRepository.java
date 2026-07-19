// repository/ScheduledPaymentRepository.java
package com.payflow.repository;

import com.payflow.entity.ScheduledPayment;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, String> {

    List<ScheduledPayment> findByOwnerOrderByNextRunAtAsc(User owner);

    List<ScheduledPayment> findByActiveTrueAndNextRunAtLessThanEqual(LocalDateTime now);
}