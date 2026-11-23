package com.audit.platform.finding.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Domain Event published to RabbitMQ (Jalon V5) when a new Finding is detected.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindingCreatedEvent implements Serializable {
    private Long findingId;
    private Long transactionId;
    private String controlCode;
    private String severity;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}

