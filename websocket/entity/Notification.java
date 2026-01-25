package com.sscl.websocket_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sscl.websocket_service.config.Paths;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = Paths.NOTIFICATION)
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private UUID id;
   @Column(name = "user_id")
    private UUID userId;
    @Column(name = "device_id")
    private UUID deviceId;
    @Column(name = "consumed_value")
    private Double consumedValue;
    @Column(name = "max_allowed_value")
    private Integer maxAllowedValue;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    @Column(name = "message")
    private String message;

}
