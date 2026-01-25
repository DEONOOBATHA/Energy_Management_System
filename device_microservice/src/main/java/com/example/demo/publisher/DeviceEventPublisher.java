package com.example.demo.publisher;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.DeviceEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishDeviceCreated(DeviceEventDTO deviceEvent) {
        log.info("Publishing device created event for device: {}", deviceEvent.getDeviceId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DEVICE_EXCHANGE,
                RabbitMQConfig.DEVICE_CREATED_KEY,
                deviceEvent
        );
    }

    public void publishDeviceUpdated(DeviceEventDTO deviceEvent) {
        log.info("Publishing device updated event for device: {}", deviceEvent.getDeviceId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DEVICE_EXCHANGE,
                RabbitMQConfig.DEVICE_UPDATED_KEY,
                deviceEvent
        );
    }

    public void publishDeviceDeleted(DeviceEventDTO deviceEvent) {
        log.info("Publishing device deleted event for device: {}", deviceEvent.getDeviceId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DEVICE_EXCHANGE,
                RabbitMQConfig.DEVICE_DELETED_KEY,
                deviceEvent
        );
    }
}
