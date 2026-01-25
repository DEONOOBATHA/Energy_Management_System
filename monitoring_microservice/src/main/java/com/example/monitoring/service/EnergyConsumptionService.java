package com.example.monitoring.service;

import com.example.monitoring.entity.EnergyConsumption;
import com.example.monitoring.repository.EnergyConsumptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnergyConsumptionService {

    private final EnergyConsumptionRepository energyConsumptionRepository;

    public List<EnergyConsumption> getUserEnergyConsumptionByDate(UUID userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        log.info("Fetching energy consumption for user: {} on date: {}", userId, date);
        return energyConsumptionRepository.findByUserIdAndDateRange(userId, startOfDay, endOfDay);
    }

    public List<EnergyConsumption> getDeviceEnergyConsumptionByDate(UUID deviceId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        log.info("Fetching energy consumption for device: {} on date: {}", deviceId, date);
        return energyConsumptionRepository.findByDeviceIdAndDateRange(deviceId, startOfDay, endOfDay);
    }
}
