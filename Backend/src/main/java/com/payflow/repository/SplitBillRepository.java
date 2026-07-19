package com.payflow.repository;

import com.payflow.entity.SplitBill;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SplitBillRepository extends JpaRepository<SplitBill, String> {

    List<SplitBill> findByOrganizerOrderByCreatedAtDesc(User organizer);

    List<SplitBill> findByStatus(SplitBill.SplitBillStatus status);
}