package com.example.monitoring.repository;

import com.example.monitoring.entity.EnergyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EnergyConsumptionRepository extends JpaRepository<EnergyConsumption, Long> {

    @Query("SELECT e FROM EnergyConsumption e WHERE e.user.id = :userId AND e.timestamp BETWEEN :startDate AND :endDate ORDER BY e.hour")
    List<EnergyConsumption> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e FROM EnergyConsumption e WHERE e.device.id = :deviceId AND e.timestamp BETWEEN :startDate AND :endDate ORDER BY e.hour")
    List<EnergyConsumption> findByDeviceIdAndDateRange(
            @Param("deviceId") UUID deviceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Delete all energy consumption records for a specific device
    void deleteByDeviceId(UUID deviceId);
}
