package com.payflow.service;

import com.payflow.dto.ai.BudgetSuggestion;
import com.payflow.dto.ai.BudgetSuggestionsResponse;
import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Suggests a next-month budget per category from the trailing 3 months of
 * actual spend, plus a 10% buffer. Simple, explainable, and doesn't require any
 * external data - this is the same "moving average + buffer" approach a
 * first-pass budgeting feature would ship with before layering in seasonality
 * or income-based modeling.
 */
@Service
@RequiredArgsConstructor
public class SmartBudgetService {

	private final TransactionRepository transactionRepository;
	private static final BigDecimal BUFFER_MULTIPLIER = new BigDecimal("1.10");

	public BudgetSuggestionsResponse suggestBudgets(User user) {
		LocalDateTime windowStart = LocalDateTime.now().minusMonths(3).withDayOfMonth(1).toLocalDate().atStartOfDay();

		List<Transaction> recent = transactionRepository.findAllForUser(user).stream()
				.filter(t -> t.getSender() != null && t.getSender().getId().equals(user.getId()))
				.filter(t -> t.getStatus() == Transaction.TransactionStatus.SUCCESS)
				.filter(t -> !t.getCreatedAt().isBefore(windowStart)).toList();

		if (recent.isEmpty()) {
			return BudgetSuggestionsResponse.builder().suggestions(List.of())
					.suggestedOverallMonthlyBudget(BigDecimal.ZERO)
					.basis("Not enough transaction history yet - suggestions will appear after your first month of activity.")
					.build();
		}

		Map<String, BigDecimal> totalByCategory = new LinkedHashMap<>();
		for (Transaction t : recent) {
			String category = t.getCategory() != null ? t.getCategory() : "Others";
			totalByCategory.merge(category, t.getAmount(), BigDecimal::add);
		}

		// Number of distinct calendar months actually covered, so a fresh account (say,
		// 3 weeks
		// of history) doesn't get its average diluted by dividing by a full 3.
		long monthsCovered = Math.max(1,
				java.time.temporal.ChronoUnit.MONTHS.between(windowStart.toLocalDate().withDayOfMonth(1),
						LocalDateTime.now().toLocalDate().withDayOfMonth(1)) + 1);

		List<BudgetSuggestion> suggestions = new ArrayList<>();
		BigDecimal overall = BigDecimal.ZERO;

		for (Map.Entry<String, BigDecimal> entry : totalByCategory.entrySet()) {
			BigDecimal average = entry.getValue().divide(BigDecimal.valueOf(monthsCovered), 2, RoundingMode.HALF_UP);
			BigDecimal suggested = average.multiply(BUFFER_MULTIPLIER).setScale(0, RoundingMode.CEILING);
			overall = overall.add(suggested);
			suggestions.add(BudgetSuggestion.builder().category(entry.getKey()).last3MonthAverage(average)
					.suggestedMonthlyBudget(suggested).build());
		}

		suggestions.sort((a, b) -> b.getSuggestedMonthlyBudget().compareTo(a.getSuggestedMonthlyBudget()));

		return BudgetSuggestionsResponse.builder().suggestions(suggestions).suggestedOverallMonthlyBudget(overall)
				.basis("Based on your average monthly spend per category over the last " + monthsCovered
						+ " month(s), plus a 10% buffer.")
				.build();
	}
}