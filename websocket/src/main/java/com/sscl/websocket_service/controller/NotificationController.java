package com.sscl.websocket_service.controller;

import com.sscl.websocket_service.dto.NotificationDto;
import com.sscl.websocket_service.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody NotificationDto dto) {
        notificationService.createAndSendNotification(dto);
        Map<String, Object> response = Map.of("message", "Notification sent successfully");
        return ResponseEntity.ok(response);
    }
}
