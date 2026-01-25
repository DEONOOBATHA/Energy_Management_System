package com.example.monitoring.consumer;

import com.example.monitoring.config.RabbitMQConfig;
import com.example.monitoring.dto.EnergyConsumptionDTO;
import com.example.monitoring.dto.OverConsumptionAlertDTO;
import com.example.monitoring.entity.Device;
import com.example.monitoring.entity.EnergyConsumption;
import com.example.monitoring.entity.Person;
import com.example.monitoring.repository.DeviceRepository;
import com.example.monitoring.repository.EnergyConsumptionRepository;
import com.example.monitoring.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.hibernate.sql.ast.tree.expression.Over;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.example.monitoring.publisher.OverConsumptionPublisher;

@Component
@Slf4j
@RequiredArgsConstructor
public class EnergyConsumptionConsumer {

    private final EnergyConsumptionRepository energyConsumptionRepository;
    private final DeviceRepository deviceRepository;
    private final PersonRepository personRepository;
    private final OverConsumptionPublisher overConsumptionPublisher;

    @RabbitListener(
        containerFactory = "dataCollectionRabbitListenerContainerFactory",
        admin = "dataCollectionRabbitAdmin",
        bindings = {
            @QueueBinding(
                value = @Queue(name = RabbitMQConfig.ENERGY_CONSUMPTION_QUEUE, durable = "true"),
                exchange = @Exchange(name = RabbitMQConfig.ENERGY_EXCHANGE, type = "topic"),
                key = "energy.consumption.#"
            )
        }
    )
    public void handleEnergyConsumption(EnergyConsumptionDTO dto) {
        log.info("Received energy consumption data for device: {}, user: {}, hour: {}, value: {} kWh",
                dto.getDeviceId(), dto.getUserId(), dto.getHour(), dto.getEnergyValue());

        try {
            // Fetch device and user entities (must exist due to sync)
            Device device = deviceRepository.findById(dto.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Device not found: " + dto.getDeviceId()));
            
            Person user = personRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + dto.getUserId()));

            EnergyConsumption consumption = EnergyConsumption.builder()
                    .device(device)
                    .user(user)
                    .energyValue(dto.getEnergyValue())
                    .timestamp(dto.getTimestamp())
                    .hour(dto.getHour())
                    .build();

            energyConsumptionRepository.save(consumption);
            if(consumption.getEnergyValue() > consumption.getDevice().getMaxConsValue()){
                log.warn("Over consumption detected for device: {}. Value: {} kWh exceeds max: {} kWh",
                        device.getId(), consumption.getEnergyValue(), device.getMaxConsValue());
                OverConsumptionAlertDTO alertDTO = new OverConsumptionAlertDTO();
                alertDTO.setDeviceId(device.getId());
                alertDTO.setUserId(user.getId());  
                alertDTO.setConsumedValue(consumption.getEnergyValue());
                // Convert types to match alert DTO expectations
                alertDTO.setMaxAllowedValue(consumption.getDevice().getMaxConsValue().doubleValue());
                alertDTO.setTimestamp(LocalDateTime.now());
                overConsumptionPublisher.publishOverConsumptionAlert(alertDTO);

            }
            log.info("Energy consumption data saved successfully");
        } catch (Exception e) {
            log.error("Error saving energy consumption data: {}", e.getMessage(), e);
        }
    }
}
