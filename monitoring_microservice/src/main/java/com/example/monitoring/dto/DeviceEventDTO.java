package com.example.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEventDTO {
    private String eventType; // CREATED, UPDATED, DELETED
    private UUID deviceId;
    private String name;
    private Integer maxConsValue;
}
