package com.example.simulator.service;

import com.example.simulator.config.RabbitMQConfig;
import com.example.simulator.dto.EnergyConsumptionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnergySimulatorService {

    private final RabbitTemplate rabbitTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();
    
    private UUID deviceId;
    private UUID userId;

    @Value("${simulator.device.id:}")
    private String deviceIdConfig;



    
    @PostConstruct
    public void init() {
        if (deviceIdConfig == null || deviceIdConfig.trim().isEmpty()) {
            log.error("DEVICE_ID must be configured!");
            log.error("Set DEVICE_ID environment variable");
            return;
        }
        
        try {
            deviceId = UUID.fromString(deviceIdConfig.trim());
            
            // Query user_id from database
            String sql = "SELECT user_id FROM user_device WHERE device_id = ?";
            userId = jdbcTemplate.queryForObject(sql, UUID.class, deviceId);
            
            if (userId == null) {
                log.error("No user association found for device: {}", deviceId);
                return;
            }
            
            log.info("Energy Simulator initialized for device: {} -> user: {}", deviceId, userId);
        } catch (Exception e) {
            log.error("Error loading device configuration: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${simulator.interval:30000}")
    public void generateAndPublishEnergyData() {
        if (deviceId == null || userId == null) {
            log.debug("Device not configured, skipping data generation");
            return;
        }

        try {
            EnergyConsumptionDTO data = generateEnergyData();
            publishEnergyData(data);
            log.info("Published: Device={}, User={}, Hour={}, Energy={} kWh",
                    data.getDeviceId(), data.getUserId(), data.getHour(), 
                    String.format("%.2f", data.getEnergyValue()));
        } catch (Exception e) {
            log.error("Error generating/publishing data: {}", e.getMessage());
        }
    }

    private EnergyConsumptionDTO generateEnergyData() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        // Base consumption: 0.5 - 2.0 kWh
        double baseConsumption = 0.5 + (random.nextDouble() * 1.5);
        
        // Time-based multiplier
        double multiplier;
        if (hour >= 6 && hour <= 22) {
            // Daytime: higher consumption (1.2x - 2.5x)
            multiplier = 1.2 + (random.nextDouble() * 1.3);
        } else {
            // Nighttime: lower consumption (0.3x - 0.8x)
            multiplier = 0.3 + (random.nextDouble() * 0.5);
        }
        
        double energyValue = baseConsumption * multiplier;
        
        return EnergyConsumptionDTO.builder()
                .deviceId(deviceId)
                .userId(userId)
                .energyValue(Math.round(energyValue * 100.0) / 100.0)
                .timestamp(now)
                .hour(hour)
                .build();
    }

    private void publishEnergyData(EnergyConsumptionDTO data) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ENERGY_EXCHANGE,
                RabbitMQConfig.ENERGY_HOURLY_KEY,
                data
        );
    }
}
