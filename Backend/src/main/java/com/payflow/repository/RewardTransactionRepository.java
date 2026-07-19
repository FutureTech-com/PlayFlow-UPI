package com.payflow.repository;

import com.payflow.entity.RewardTransaction;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, String> {

    List<RewardTransaction> findByUserOrderByCreatedAtDesc(User user);

    List<RewardTransaction> findByUserAndStatus(User user, RewardTransaction.RewardStatus status);

    List<RewardTransaction> findByRelatedTransactionId(String relatedTransactionId);
}