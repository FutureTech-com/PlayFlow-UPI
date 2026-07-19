package com.payflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "complaints")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String subject;

    @Column(length = 2000)
    private String description;

    private String relatedTransactionId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.OPEN;

    // Optional link to a transaction the complaint is about
    @Column(name = "transaction_id")
    private String transactionId;
    
    private String resolutionNote;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant resolvedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = ComplaintStatus.OPEN;
        }
    }

    public enum ComplaintStatus {
        OPEN, IN_REVIEW, RESOLVED, REJECTED
    }
}