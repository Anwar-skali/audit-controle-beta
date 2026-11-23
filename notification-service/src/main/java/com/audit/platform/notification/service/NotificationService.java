package com.audit.platform.notification.service;

import com.audit.platform.notification.dto.NotificationResponseDTO;
import com.audit.platform.notification.event.FindingCreatedEvent;

import java.util.List;

public interface NotificationService {

    NotificationResponseDTO processFindingEvent(FindingCreatedEvent event);

    List<NotificationResponseDTO> getAllNotifications();

    List<NotificationResponseDTO> getNotificationsByFindingId(Long findingId);

    NotificationResponseDTO getNotificationById(Long id);
}

