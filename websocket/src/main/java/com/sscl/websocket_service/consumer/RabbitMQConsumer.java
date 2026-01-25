package com.sscl.websocket_service.consumer;

import com.sscl.websocket_service.config.Paths;
import com.sscl.websocket_service.dto.NotificationDto;
import com.sscl.websocket_service.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RabbitMQConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private final NotificationService notificationService;

    public RabbitMQConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = Paths.NOTIFICATION_QUEUE)
    public void consume(NotificationDto dto) {
        log.info("Received Notification for device: {} and user: {}", dto.getDeviceId(), dto.getUserId());
        try {
            notificationService.createAndSendNotification(dto);
            log.info("Notification processed and sent via WebSocket");
        } catch (Exception e) {
            log.error("Failed to process notification: {}", dto, e);
        }
    }
}
