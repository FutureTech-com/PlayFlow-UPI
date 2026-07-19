package com.payflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "billers")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Biller{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillerCategory category;
    
    @Column(nullable = false, unique = true)
    private String code;              // e.g. "AIRTEL_PREPAID", "JIO_PREPAID"

    private String logoUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean active=false;

    @Column(precision = 12, scale = 2)
    private BigDecimal minAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false, updatable =false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (!this.active) {
            this.active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
    
    public enum BillerCategory {
        MOBILE_PREPAID,
        MOBILE_POSTPAID,
        DTH,
        BROADBAND,
        LANDLINE,
        ELECTRICITY,
        WATER,
        GAS,
        INSURANCE,
        CREDIT_CARD,
        LOAN_EMI,
        FASTAG,
        SUBSCRIPTION,
        OTHER
    }    
}