package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "energy_consumption")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Person user;

    @Column(nullable = false)
    private Double energyValue; // kWh

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Integer hour; // 0-23

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
