package com.payflow.service;

import com.payflow.dto.reward.RewardSummaryResponse;
import com.payflow.entity.RewardTransaction;
import com.payflow.entity.User;
import com.payflow.repository.RewardTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardService {

    private final RewardTransactionRepository rewardTransactionRepository;

    public RewardSummaryResponse getSummary(User user) {
        List<RewardTransaction> all = rewardTransactionRepository.findByUserOrderByCreatedAtDesc(user);

        BigDecimal totalCashback = all.stream().map(RewardTransaction::getCashbackAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalPoints = all.stream().mapToInt(RewardTransaction::getPoints).sum();

        List<RewardSummaryResponse.RewardEntry> history = all.stream()
                .map(r -> RewardSummaryResponse.RewardEntry.builder()
                        .id(r.getId())
                        .cashbackAmount(r.getCashbackAmount())
                        .points(r.getPoints() != null ? r.getPoints() : 0)
                        .reason(r.getReason())
                        .createdAt(java.time.LocalDateTime.ofInstant(r.getCreatedAt(), java.time.ZoneOffset.UTC))
                        .build())
                .toList();

        return RewardSummaryResponse.builder()
                .totalCashback(totalCashback)
                .totalPoints(totalPoints)
                .history(history)
                .build();
    }
}
