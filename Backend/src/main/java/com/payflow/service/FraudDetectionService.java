package com.payflow.service;

import com.payflow.dto.ai.FraudAlertResponse;
import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

	private final TransactionRepository transactionRepository;

	@Value("${payflow.fraud.high-risk-threshold:70}")
	private int highRiskThreshold;

	@Value("${payflow.fraud.medium-risk-threshold:35}")
	private int mediumRiskThreshold;

	public record FraudAssessment(int score, Transaction.RiskLevel level, List<String> reasons) {
	}

	public FraudAssessment assess(User sender, BigDecimal amount, String receiverVpa) {
		List<String> reasons = new ArrayList<>();
		int score = 0;

		List<Transaction> history = transactionRepository.findAllForUser(sender).stream()
				.filter(t -> t.getSender() != null && t.getSender().getId().equals(sender.getId()))
				.filter(t -> t.getStatus() == Transaction.TransactionStatus.SUCCESS).toList();

		// Signal 1: amount vs the sender's own historical average send amount.
		if (!history.isEmpty()) {
			BigDecimal avg = history.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
					.divide(BigDecimal.valueOf(history.size()), 2, RoundingMode.HALF_UP);

			if (avg.compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal ratio = amount.divide(avg, 2, RoundingMode.HALF_UP);
				if (ratio.compareTo(BigDecimal.valueOf(10)) >= 0) {
					score += 40;
					reasons.add("Amount is over 10x your typical payment (avg ₹" + avg + ")");
				} else if (ratio.compareTo(BigDecimal.valueOf(5)) >= 0) {
					score += 25;
					reasons.add("Amount is over 5x your typical payment (avg ₹" + avg + ")");
				} else if (ratio.compareTo(BigDecimal.valueOf(3)) >= 0) {
					score += 10;
					reasons.add("Amount is noticeably higher than your typical payment");
				}
			}
		} else if (amount.compareTo(BigDecimal.valueOf(5000)) > 0) {
			// No history at all yet, and already sending a large amount.
			score += 15;
			reasons.add("Large first payment with no prior transaction history");
		}

		// Signal 2: brand-new recipient (never paid before).
		boolean paidBefore = history.stream().anyMatch(t -> receiverVpa.equalsIgnoreCase(t.getReceiverVpa()));
		if (!paidBefore) {
			score += 15;
			reasons.add("First time paying this recipient");
		}

		// Signal 3: odd hour (11pm - 5am), a classic ATO/account-takeover signal.
		LocalTime now = LocalDateTime.now().toLocalTime();
		if (now.isAfter(LocalTime.of(23, 0)) || now.isBefore(LocalTime.of(5, 0))) {
			score += 15;
			reasons.add("Payment initiated late at night");
		}

		// Signal 4: velocity in the last 10 minutes (separate from PaymentService's
		// hard block,
		// this contributes to the score even below that hard limit).
		long recentCount = transactionRepository.countBySenderSince(sender, LocalDateTime.now().minusMinutes(10));
		if (recentCount >= 3) {
			score += 20;
			reasons.add(recentCount + " payments sent in the last 10 minutes");
		}

		// Signal 5: round, large amount - a common pattern in social-engineering scams
		// ("send exactly 50000 to secure your account", etc.)
		if (amount.compareTo(BigDecimal.valueOf(10000)) >= 0
				&& amount.remainder(BigDecimal.valueOf(1000)).compareTo(BigDecimal.ZERO) == 0) {
			score += 5;
			reasons.add("Round, high-value amount");
		}

		score = Math.min(score, 100);

		Transaction.RiskLevel level;
		if (score >= highRiskThreshold) {
			level = Transaction.RiskLevel.HIGH;
		} else if (score >= mediumRiskThreshold) {
			level = Transaction.RiskLevel.MEDIUM;
		} else {
			level = Transaction.RiskLevel.LOW;
			if (reasons.isEmpty())
				reasons.add("No risk signals detected");
		}

		return new FraudAssessment(score, level, reasons);
	}

	/**
	 * Shared mapper from a persisted Transaction's risk fields to the API-facing
	 * DTO.
	 */
	public static FraudAlertResponse toAlert(Transaction t, boolean includeUserName) {
		List<String> reasons = t.getRiskReasons() != null && !t.getRiskReasons().isBlank()
				? List.of(t.getRiskReasons().split("\\|"))
				: List.of();
		return FraudAlertResponse.builder().transactionId(t.getId()).referenceId(t.getReferenceId())
				.userFullName(includeUserName && t.getSender() != null ? t.getSender().getFullName() : null)
				.amount(t.getAmount()).counterpartyVpa(t.getReceiverVpa())
				.riskScore(t.getRiskScore() != null ? t.getRiskScore() : 0)
				.riskLevel(t.getRiskLevel() != null ? t.getRiskLevel().name() : "LOW").reasons(reasons)
				.timestamp(t.getCreatedAt()).build();
	}
}