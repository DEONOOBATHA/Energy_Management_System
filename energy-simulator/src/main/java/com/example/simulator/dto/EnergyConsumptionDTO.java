package com.example.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyConsumptionDTO {
    private UUID deviceId;
    private UUID userId;
    private Double energyValue;  // kWh
    private LocalDateTime timestamp;
    private Integer hour;        // 0-23
}
