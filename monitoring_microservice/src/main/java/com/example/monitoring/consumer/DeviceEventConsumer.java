package com.example.monitoring.consumer;

import com.example.monitoring.config.RabbitMQConfig;
import com.example.monitoring.dto.DeviceEventDTO;
import com.example.monitoring.entity.Device;
import com.example.monitoring.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeviceEventConsumer {

    private final DeviceRepository deviceRepository;

    @RabbitListener(
        containerFactory = "rabbitListenerContainerFactory",
        bindings = {
            @QueueBinding(
                value = @Queue(name = RabbitMQConfig.DEVICE_SYNC_QUEUE, durable = "true"),
                exchange = @Exchange(name = RabbitMQConfig.DEVICE_EXCHANGE, type = "topic"),
                key = {"device.created", "device.updated", "device.deleted"}
            )
        }
    )
    public void handleDeviceEvent(DeviceEventDTO event) {
        log.info("Received device event: {} for device ID: {}", event.getEventType(), event.getDeviceId());

        try {
            switch (event.getEventType()) {
                case "CREATED":
                    handleDeviceCreated(event);
                    break;
                case "UPDATED":
                    handleDeviceUpdated(event);
                    break;
                case "DELETED":
                    handleDeviceDeleted(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing device event: {}", e.getMessage(), e);
        }
    }

    private void handleDeviceCreated(DeviceEventDTO event) {
        Device device = Device.builder()
                .id(event.getDeviceId())
                .name(event.getName())
                .maxConsValue(event.getMaxConsValue())
                .build();
        
        deviceRepository.save(device);
        log.info("Device synchronized: {}", device.getId());
    }

    private void handleDeviceUpdated(DeviceEventDTO event) {
        deviceRepository.findById(event.getDeviceId()).ifPresent(device -> {
            device.setName(event.getName());
            device.setMaxConsValue(event.getMaxConsValue());
            deviceRepository.save(device);
            log.info("Device updated: {}", device.getId());
        });
    }

    private void handleDeviceDeleted(DeviceEventDTO event) {
        deviceRepository.deleteById(event.getDeviceId());
        log.info("Device deleted: {}", event.getDeviceId());
    }
}
