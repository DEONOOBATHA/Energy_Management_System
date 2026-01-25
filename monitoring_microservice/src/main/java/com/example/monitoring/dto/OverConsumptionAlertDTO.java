package com.example.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverConsumptionAlertDTO {
    private UUID deviceId;
    private UUID userId;
    private String deviceName;
    private Double consumedValue;
    private Double maxAllowedValue;
    private Double exceededBy;
    private Integer hour;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}