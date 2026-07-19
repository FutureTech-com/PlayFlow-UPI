package com.payflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reward_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Links back to the payment/wallet transaction that triggered this reward
    private String relatedTransactionId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RewardType type = RewardType.CASHBACK;

    // Monetary cashback component (e.g. ₹5.00)
    @Column(precision = 19, scale = 2)
    private BigDecimal cashbackAmount;

    // Non-monetary loyalty points component, if you also track points separately from cashback
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RewardStatus status = RewardStatus.CREDITED;

    private String reason;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public enum RewardType {
        CASHBACK, REFERRAL_BONUS, PROMO, REDEEMED, EXPIRED
    }

    public enum RewardStatus {
        PENDING, CREDITED, REVERSED, EXPIRED
    }
}