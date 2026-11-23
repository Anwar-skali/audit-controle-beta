package com.audit.platform.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    private Long id;
    private Long findingId;
    private Long transactionId;
    private String controlCode;
    private String severity;
    private String recipientEmail;
    private String subject;
    private String message;
    private String status; // SENT, SIMULATED, FAILED
    private LocalDateTime sentAt;
}

