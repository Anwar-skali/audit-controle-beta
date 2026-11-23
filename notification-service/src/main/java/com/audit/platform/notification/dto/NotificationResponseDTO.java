package com.audit.platform.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {
    private Long id;
    private Long findingId;
    private Long transactionId;
    private String controlCode;
    private String severity;
    private String recipientEmail;
    private String subject;
    private String message;
    private String status;
    private LocalDateTime sentAt;
}

