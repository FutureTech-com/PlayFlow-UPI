package com.payflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "trusted_devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustedDevice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String deviceId;

    private String deviceName;
    
    @Column(nullable = false)
    private String deviceFingerprint; // hash of User-Agent + IP in this demo
    private String userAgent;
    private String lastIp;
    
    @Column(nullable = false, updatable = false)
    private Instant trustedAt;

    private Instant lastSeenAt;

    
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        trustedAt = now;
        lastSeenAt = now;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrustedDevice other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}