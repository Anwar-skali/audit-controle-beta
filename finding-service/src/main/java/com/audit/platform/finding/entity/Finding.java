package com.audit.platform.finding.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "findings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Finding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Remote logical reference to Transaction in transaction-service.
     * No JPA cross-database foreign key or relationship.
     * Existence is validated via OpenFeign in V4.
     */
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "control_code", nullable = false, length = 100)
    private String controlCode;

    @Column(nullable = false, length = 30)
    private String severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "OPEN";
        }
    }
}

