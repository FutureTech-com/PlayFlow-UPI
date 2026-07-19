package com.payflow.service;

import com.payflow.dto.ai.CategoryBreakdownItem;
import com.payflow.dto.ai.SpendingInsightsResponse;
import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns categorized transaction history into plain-English observations. Still
 * no external LLM call - the "AI" here is a small set of comparison rules (this
 * month vs last month, per category) worded as natural sentences. Swapping this
 * for a real LLM call later would only mean replacing generateNarrative() with
 * a prompt built from the same aggregates.
 */

@Service
@RequiredArgsConstructor
public class SpendingInsightsService {

	private final TransactionRepository transactionRepository;

	public SpendingInsightsResponse getInsights(User user) {
		LocalDate today = LocalDate.now();
		LocalDateTime currentMonthStart = today.withDayOfMonth(1).atStartOfDay();
		LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);

		List<Transaction> sent = transactionRepository.findAllForUser(user).stream()
				.filter(t -> t.getSender() != null && t.getSender().getId().equals(user.getId()))
				.filter(t -> t.getStatus() == Transaction.TransactionStatus.SUCCESS).toList();

		List<Transaction> currentMonth = sent.stream().filter(t -> !t.getCreatedAt().isBefore(currentMonthStart))
				.toList();
		List<Transaction> previousMonth = sent.stream().filter(
				t -> !t.getCreatedAt().isBefore(previousMonthStart) && t.getCreatedAt().isBefore(currentMonthStart))
				.toList();

		BigDecimal currentTotal = sumOf(currentMonth);
		BigDecimal previousTotal = sumOf(previousMonth);

		Map<String, BigDecimal> currentByCategory = groupByCategory(currentMonth);
		Map<String, BigDecimal> previousByCategory = groupByCategory(previousMonth);
		Map<String, Long> currentCountByCategory = countByCategory(currentMonth);

		List<CategoryBreakdownItem> breakdown = new ArrayList<>();
		for (Map.Entry<String, BigDecimal> entry : currentByCategory.entrySet()) {
			double percent = currentTotal.compareTo(BigDecimal.ZERO) == 0 ? 0.0
					: entry.getValue().divide(currentTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
			breakdown.add(CategoryBreakdownItem.builder().category(entry.getKey()).totalSpent(entry.getValue())
					.transactionCount(currentCountByCategory.getOrDefault(entry.getKey(), 0L))
					.percentOfTotal(Math.round(percent * 10.0) / 10.0).build());
		}
		breakdown.sort((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()));

		List<String> insights = generateNarrative(currentTotal, previousTotal, currentByCategory, previousByCategory,
				currentMonth);

		return SpendingInsightsResponse.builder().insights(insights).currentMonthTotal(currentTotal)
				.previousMonthTotal(previousTotal).categoryBreakdown(breakdown).build();
	}

	private List<String> generateNarrative(BigDecimal currentTotal, BigDecimal previousTotal,
			Map<String, BigDecimal> currentByCategory, Map<String, BigDecimal> previousByCategory,
			List<Transaction> currentMonth) {
		List<String> insights = new ArrayList<>();

		if (currentMonth.isEmpty()) {
			insights.add("No spending recorded yet this month - insights will appear once you make a few payments.");
			return insights;
		}

		// Month-over-month overall trend.
		if (previousTotal.compareTo(BigDecimal.ZERO) > 0) {
			double changePercent = currentTotal.subtract(previousTotal).divide(previousTotal, 4, RoundingMode.HALF_UP)
					.doubleValue() * 100.0;
			if (Math.abs(changePercent) >= 5) {
				String direction = changePercent > 0 ? "up" : "down";
				insights.add(String.format("Your spending is %s %.0f%% compared to last month (₹%s vs ₹%s).", direction,
						Math.abs(changePercent), currentTotal, previousTotal));
			} else {
				insights.add("Your spending is about the same as last month.");
			}
		}

		// Category that grew the most.
		String biggestIncreaseCategory = null;
		double biggestIncreasePercent = 0;
		for (Map.Entry<String, BigDecimal> entry : currentByCategory.entrySet()) {
			BigDecimal prev = previousByCategory.getOrDefault(entry.getKey(), BigDecimal.ZERO);
			if (prev.compareTo(BigDecimal.ZERO) == 0)
				continue;
			double change = entry.getValue().subtract(prev).divide(prev, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
			if (change > biggestIncreasePercent) {
				biggestIncreasePercent = change;
				biggestIncreaseCategory = entry.getKey();
			}
		}
		if (biggestIncreaseCategory != null && biggestIncreasePercent >= 20) {
			insights.add(String.format("You spent %.0f%% more on %s this month than last month.",
					biggestIncreasePercent, biggestIncreaseCategory));
		}

		// Top spending category this month.
		currentByCategory.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(top -> {
			double share = currentTotal.compareTo(BigDecimal.ZERO) == 0 ? 0
					: top.getValue().divide(currentTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
			insights.add(String.format("%s is your biggest spending category this month at ₹%s (%.0f%% of total).",
					top.getKey(), top.getValue(), share));
		});

		return insights;
	}

	private Map<String, BigDecimal> groupByCategory(List<Transaction> transactions) {
		Map<String, BigDecimal> map = new LinkedHashMap<>();
		for (Transaction t : transactions) {
			String category = t.getCategory() != null ? t.getCategory() : "Others";
			map.merge(category, t.getAmount(), BigDecimal::add);
		}
		return map;
	}

	@SuppressWarnings("null")
	private Map<String, Long> countByCategory(List<Transaction> transactions) {
		Map<String, Long> map = new LinkedHashMap<>();
		for (Transaction t : transactions) {
			String category = t.getCategory() != null ? t.getCategory() : "Others";
			map.merge(category, 1L, Long::sum);
		}
		return map;
	}

	private BigDecimal sumOf(List<Transaction> transactions) {
		return transactions.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}