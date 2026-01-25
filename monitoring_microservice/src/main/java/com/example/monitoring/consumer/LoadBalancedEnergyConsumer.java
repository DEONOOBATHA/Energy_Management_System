package com.example.monitoring.consumer;

import com.example.monitoring.dto.EnergyConsumptionDTO;
import com.example.monitoring.dto.OverConsumptionAlertDTO;
import com.example.monitoring.entity.Device;
import com.example.monitoring.entity.EnergyConsumption;
import com.example.monitoring.entity.Person;
import com.example.monitoring.repository.DeviceRepository;
import com.example.monitoring.repository.EnergyConsumptionRepository;
import com.example.monitoring.repository.PersonRepository;
import com.example.monitoring.publisher.OverConsumptionPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoadBalancedEnergyConsumer {

    private final EnergyConsumptionRepository energyConsumptionRepository;
    private final DeviceRepository deviceRepository;
    private final PersonRepository personRepository;
    private final OverConsumptionPublisher overConsumptionPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${replica.id:1}")
    private int replicaId;

    /**
     * Consume from replica-specific ingest queue created by load balancer
     * Queue name format: monitoring.ingest.1, monitoring.ingest.2, etc.
     */
    @RabbitListener(
        containerFactory = "dataCollectionRabbitListenerContainerFactory",
        admin = "dataCollectionRabbitAdmin",
        queues = "${load-balancer.ingest-queue:monitoring.ingest.${replica.id:1}}"
    )
    public void handleLoadBalancedEnergyData(String message) {
        log.info("Replica {} received energy data: {}", replicaId, message);

        try {
            // Parse JSON message
            EnergyConsumptionDTO dto = objectMapper.readValue(message, EnergyConsumptionDTO.class);

            log.info("Processing energy consumption for device: {}, user: {}, hour: {}, value: {} kWh",
                    dto.getDeviceId(), dto.getUserId(), dto.getHour(), dto.getEnergyValue());

            // Fetch device and user entities
            Device device = deviceRepository.findById(dto.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Device not found: " + dto.getDeviceId()));

            Person user = personRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + dto.getUserId()));

            // Save energy consumption record
            EnergyConsumption consumption = new EnergyConsumption();
            consumption.setDevice(device);
            consumption.setUser(user);
            consumption.setTimestamp(LocalDateTime.now());
            consumption.setHour(dto.getHour());
            consumption.setEnergyValue(dto.getEnergyValue());

            energyConsumptionRepository.save(consumption);

            log.info("Saved energy consumption record for device {}, hour {}", dto.getDeviceId(), dto.getHour());

            // Check for over-consumption
            if (dto.getEnergyValue() > device.getMaxConsValue()) {
                double exceededBy = dto.getEnergyValue() - device.getMaxConsValue();
                log.warn("⚠️ OVER-CONSUMPTION detected for device {} - Exceeded by {} kWh",
                        dto.getDeviceId(), exceededBy);

                // Publish alert via WebSocket
                OverConsumptionAlertDTO alert = new OverConsumptionAlertDTO(
                        dto.getDeviceId(),
                        dto.getUserId(),
                        device.getName(),
                        dto.getEnergyValue(),
                        device.getMaxConsValue().doubleValue(),
                        exceededBy,
                        dto.getHour(),
                        LocalDateTime.now()
                );

                overConsumptionPublisher.publishOverConsumptionAlert(alert);
                log.info("Published over-consumption alert for device {}", dto.getDeviceId());
            }

        } catch (Exception e) {
            log.error("Error processing load-balanced energy data: {}", message, e);
            throw new RuntimeException("Energy data processing failed", e);
        }
    }
}
