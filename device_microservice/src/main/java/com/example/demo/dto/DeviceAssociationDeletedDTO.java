package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAssociationDeletedDTO {
    private UUID deviceId;
    private UUID userId;
    private Long associationId;
}
