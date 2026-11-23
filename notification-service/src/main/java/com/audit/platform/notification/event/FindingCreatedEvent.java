package com.audit.platform.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

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

