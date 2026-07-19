package com.payflow.repository;

import com.payflow.entity.SplitBill;
import com.payflow.entity.SplitBillShare;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SplitBillShareRepository extends JpaRepository<SplitBillShare, String> {

    List<SplitBillShare> findBySplitBill(SplitBill splitBill);

    List<SplitBillShare> findByParticipantOrderByCreatedAtDesc(User participant);

    List<SplitBillShare> findByParticipantAndSettled(User participant, boolean settled);

    long countBySplitBillAndSettled(SplitBill splitBill, boolean settled);
}