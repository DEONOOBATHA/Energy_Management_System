package com.example.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDTO {
    private String eventType; // CREATED, UPDATED, DELETED
    private UUID userId;
    private String name;
    private String address;
    private Integer age;
}
