package com.audit.platform.notification.consumer;

import com.audit.platform.notification.event.FindingCreatedEvent;
import com.audit.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ Consumer for Jalon V5.
 * Listens to finding.created events and triggers email notification workflow.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FindingNotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${audit.rabbitmq.queue:audit.findings.queue}")
    public void consumeFindingCreatedEvent(FindingCreatedEvent event) {
        log.info("Received FindingCreatedEvent via RabbitMQ for findingId: {}, severity: {}",
                event.getFindingId(), event.getSeverity());
        try {
            notificationService.processFindingEvent(event);
            log.info("FindingCreatedEvent processed successfully for findingId: {}", event.getFindingId());
        } catch (Exception e) {
            log.error("Error processing FindingCreatedEvent: {}", e.getMessage(), e);
        }
    }
}

