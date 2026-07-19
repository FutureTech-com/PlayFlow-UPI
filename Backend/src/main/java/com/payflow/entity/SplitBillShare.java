package com.payflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "split_bill_shares")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitBillShare{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "split_bill_id", nullable = false)
    private SplitBill splitBill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountOwed;

    @Builder.Default
    private boolean settled = false;

    private String settledTransactionId;

    private Instant settledAt;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}