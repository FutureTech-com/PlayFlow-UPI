package com.payflow.service;

import com.payflow.dto.analytics.SpendingAnalyticsResponse;
import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    public SpendingAnalyticsResponse getAnalytics(User user) {
        List<Transaction> all = transactionRepository.findAllForUser(user).stream()
                .filter(t -> t.getStatus() == Transaction.TransactionStatus.SUCCESS)
                .toList();

        LocalDateTime nowMonthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();

        BigDecimal spentThisMonth = BigDecimal.ZERO;
        BigDecimal receivedThisMonth = BigDecimal.ZERO;

        Map<String, BigDecimal[]> byMonth = new LinkedHashMap<>(); // month -> [spent, received]
        Map<String, BigDecimal> byType = new LinkedHashMap<>();

        for (Transaction t : all) {
            boolean isSender = t.getSender() != null && t.getSender().getId().equals(user.getId());
            String month = t.getCreatedAt().format(MONTH_FORMAT);

            byMonth.computeIfAbsent(month, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (isSender) {
                byMonth.get(month)[0] = byMonth.get(month)[0].add(t.getAmount());
                byType.merge(t.getTransactionType().name(), t.getAmount(), BigDecimal::add);
                if (!t.getCreatedAt().isBefore(nowMonthStart)) {
                    spentThisMonth = spentThisMonth.add(t.getAmount());
                }
            } else {
                byMonth.get(month)[1] = byMonth.get(month)[1].add(t.getAmount());
                if (!t.getCreatedAt().isBefore(nowMonthStart)) {
                    receivedThisMonth = receivedThisMonth.add(t.getAmount());
                }
            }
        }

        List<SpendingAnalyticsResponse.MonthlyTotal> last6 = byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .skip(Math.max(0, byMonth.size() - 6))
                .map(e -> SpendingAnalyticsResponse.MonthlyTotal.builder()
                        .month(e.getKey())
                        .spent(e.getValue()[0])
                        .received(e.getValue()[1])
                        .build())
                .toList();

        return SpendingAnalyticsResponse.builder()
                .totalSpentThisMonth(spentThisMonth)
                .totalReceivedThisMonth(receivedThisMonth)
                .last6Months(last6)
                .byType(byType)
                .build();
    }
}
