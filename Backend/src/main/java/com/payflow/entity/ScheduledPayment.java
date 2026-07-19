package com.payflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String receiverVpa;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recurrence recurrence;

    @Column(nullable = false)
    private LocalDateTime nextRunAt;

    private LocalDateTime lastRunAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    private String lastFailureReason;

    public enum Recurrence {
        ONCE, DAILY, WEEKLY, MONTHLY
    }
}