package com.sscl.websocket_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sscl.websocket_service.config.Paths;
import com.sscl.websocket_service.dto.NotificationDto;
import com.sscl.websocket_service.entity.Notification;
import com.sscl.websocket_service.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository notificationRepository, 
                             SimpMessagingTemplate messagingTemplate, 
                             ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createAndSendNotification(NotificationDto dto) {
        String messageText = String.format(
                "Alert: Consumption limit exceeded for device %s. Consumed: %.2f kWh, Max Allowed: %.2f kWh",
                dto.getDeviceId(),
                dto.getConsumedValue(),
                dto.getMaxAllowedValue()
        );

        Notification notification = Notification.builder()
                .userId(dto.getUserId())
                .deviceId(dto.getDeviceId())
                .consumedValue(dto.getConsumedValue())
                .maxAllowedValue(dto.getMaxAllowedValue())
                .timestamp(java.time.LocalDateTime.now())
                .message(messageText)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification saved with ID: {} for device: {} and user: {}", 
                savedNotification.getId(), dto.getDeviceId(), dto.getUserId());

        // Broadcast notification via WebSocket to all connected frontend clients
        sendNotificationToWebSocket(dto);
        log.info("Notification broadcasted via WebSocket to /topic/notifications");
    }

private void sendNotificationToWebSocket(NotificationDto dto) {
    try {
        messagingTemplate.convertAndSend(
                Paths.TOPIC_NOTIFICATIONS,
                dto  // NU mai face writeValueAsString!
        );
        log.debug("WebSocket message sent to topic: {} | Device: {}, Consumed: {} kWh", 
                Paths.TOPIC_NOTIFICATIONS, dto.getDeviceId(), dto.getConsumedValue());
    } catch (Exception e) {
        log.error("Failed to send WebSocket notification for device ID: {} - {}", 
                dto.getDeviceId(), e.getMessage(), e);
    }
}
}
