package com.audit.platform.notification.service.impl;

import com.audit.platform.notification.dto.NotificationResponseDTO;
import com.audit.platform.notification.event.FindingCreatedEvent;
import com.audit.platform.notification.mail.EmailService;
import com.audit.platform.notification.model.Notification;
import com.audit.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;
    private final ConcurrentHashMap<Long, Notification> notifications = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Value("${audit.notification.from-email:no-reply@audit-platform.internal}")
    private String fromEmail;

    @Value("${audit.notification.default-auditor-email:auditor@audit-platform.internal}")
    private String defaultAuditorEmail;

    @Override
    public NotificationResponseDTO processFindingEvent(FindingCreatedEvent event) {
        log.info("Processing FindingCreatedEvent for findingId: {}, severity: {}",
                event.getFindingId(), event.getSeverity());

        Long notifId = idSequence.getAndIncrement();
        String subject = String.format("[ALERTE AUDIT %s] Nouveau Constat #%d - Contrôle %s",
                event.getSeverity(), event.getFindingId(), event.getControlCode());
        String body = String.format(
                "Alerte d'audit continu :\n" +
                "- ID Constat : %d\n" +
                "- ID Transaction : %d\n" +
                "- Code Contrôle : %s\n" +
                "- Sévérité : %s\n" +
                "- Description : %s\n" +
                "- Date de détection : %s\n\n" +
                "Veuillez vous connecter à la Plateforme d'Audit pour investiguer.",
                event.getFindingId(), event.getTransactionId(), event.getControlCode(),
                event.getSeverity(), event.getDescription(), event.getCreatedAt()
        );

        boolean emailSent = emailService.sendEmail(fromEmail, defaultAuditorEmail, subject, body);

        Notification notification = Notification.builder()
                .id(notifId)
                .findingId(event.getFindingId())
                .transactionId(event.getTransactionId())
                .controlCode(event.getControlCode())
                .severity(event.getSeverity())
                .recipientEmail(defaultAuditorEmail)
                .subject(subject)
                .message(body)
                .status(emailSent ? "SENT" : "SIMULATED")
                .sentAt(LocalDateTime.now())
                .build();

        notifications.put(notifId, notification);
        log.info("Notification record created with id: {}", notifId);

        return mapToDTO(notification);
    }

    @Override
    public List<NotificationResponseDTO> getAllNotifications() {
        return notifications.values().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponseDTO> getNotificationsByFindingId(Long findingId) {
        return notifications.values().stream()
                .filter(n -> findingId.equals(n.getFindingId()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponseDTO getNotificationById(Long id) {
        Notification notification = notifications.get(id);
        return notification != null ? mapToDTO(notification) : null;
    }

    private NotificationResponseDTO mapToDTO(Notification n) {
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .findingId(n.getFindingId())
                .transactionId(n.getTransactionId())
                .controlCode(n.getControlCode())
                .severity(n.getSeverity())
                .recipientEmail(n.getRecipientEmail())
                .subject(n.getSubject())
                .message(n.getMessage())
                .status(n.getStatus())
                .sentAt(n.getSentAt())
                .build();
    }
}

