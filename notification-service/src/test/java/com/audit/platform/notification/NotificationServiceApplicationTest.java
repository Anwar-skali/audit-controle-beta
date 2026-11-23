package com.audit.platform.notification;

import com.audit.platform.notification.dto.NotificationResponseDTO;
import com.audit.platform.notification.event.FindingCreatedEvent;
import com.audit.platform.notification.mail.EmailService;
import com.audit.platform.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceApplicationTest {

    @Autowired
    private NotificationService notificationService;

    @MockBean
    private EmailService emailService;

    @Test
    @DisplayName("Context loads and processes FindingCreatedEvent into stored Notification (V5)")
    void contextLoads_AndProcessesEvent() {
        when(emailService.sendEmail(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        FindingCreatedEvent event = FindingCreatedEvent.builder()
                .findingId(10L)
                .transactionId(200L)
                .controlCode("CTRL_AML_001")
                .severity("CRITICAL")
                .description("Transaction suspected of money laundering pattern")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        NotificationResponseDTO response = notificationService.processFindingEvent(event);

        assertThat(response).isNotNull();
        assertThat(response.getFindingId()).isEqualTo(10L);
        assertThat(response.getSeverity()).isEqualTo("CRITICAL");
        assertThat(response.getStatus()).isEqualTo("SENT");

        List<NotificationResponseDTO> list = notificationService.getAllNotifications();
        assertThat(list).isNotEmpty();
    }
}

