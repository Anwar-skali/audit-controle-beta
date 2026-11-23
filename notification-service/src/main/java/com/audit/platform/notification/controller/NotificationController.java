package com.audit.platform.notification.controller;

import com.audit.platform.notification.dto.NotificationResponseDTO;
import com.audit.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/notifications", "/notifications"})
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications(
            @RequestParam(required = false) Long findingId) {
        if (findingId != null) {
            log.info("REST request to get Notifications for findingId: {}", findingId);
            return ResponseEntity.ok(notificationService.getNotificationsByFindingId(findingId));
        }
        log.info("REST request to get all Notifications");
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(@PathVariable Long id) {
        log.info("REST request to get Notification by id: {}", id);
        NotificationResponseDTO response = notificationService.getNotificationById(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}

