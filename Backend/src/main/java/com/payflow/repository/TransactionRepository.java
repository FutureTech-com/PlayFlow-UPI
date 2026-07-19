package com.payflow.repository;

import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
	Optional<Transaction> findByReferenceId(String referenceId);

	@Query("SELECT t FROM Transaction t WHERE t.sender = :user OR t.receiver = :user ORDER BY t.createdAt DESC")
	List<Transaction> findAllForUser(@Param("user") User user);

	@Query("SELECT COUNT(t) FROM Transaction t WHERE t.sender = :user AND t.createdAt >= :since")
	long countBySenderSince(@Param("user") User user, @Param("since") LocalDateTime since);

	// Admin - all fraud transactions

	List<Transaction> findByRiskLevelInOrderByRiskScoreDesc(List<Transaction.RiskLevel> riskLevels);

	// User - only that user's fraud transactions
	/**
	 * Same as above, scoped to one user's own sent transactions - used by
	 * /api/ai/fraud-alerts.
	 */
	List<Transaction> findBySenderAndRiskLevelInOrderByRiskScoreDesc(User sender,
			List<Transaction.RiskLevel> riskLevels);

}