package com.sscl.websocket_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto implements Serializable {
   
    private UUID userId;

    private UUID deviceId;

    private Double consumedValue;

    private Integer maxAllowedValue;

    private LocalDateTime timestamp;

    private String message;
}

