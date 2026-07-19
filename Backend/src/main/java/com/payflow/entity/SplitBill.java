package com.payflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "split_bills")
@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class SplitBill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SplitBillStatus status = SplitBillStatus.OPEN;

    private String sourceTransactionId;

    @OneToMany(mappedBy = "splitBill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SplitBillShare> shares = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    private Instant settledAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = SplitBillStatus.OPEN;
        }
    }

    public enum SplitBillStatus {
        OPEN, PARTIALLY_PAID, SETTLED, CANCELLED
    }
}