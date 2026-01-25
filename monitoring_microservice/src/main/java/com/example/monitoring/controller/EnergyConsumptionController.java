package com.example.monitoring.controller;

import com.example.monitoring.entity.EnergyConsumption;
import com.example.monitoring.service.EnergyConsumptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/energy")
@RequiredArgsConstructor
@Tag(name = "Energy Consumption", description = "Operations for retrieving energy consumption data")
public class EnergyConsumptionController {

    private final EnergyConsumptionService energyConsumptionService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's energy consumption for a specific date", 
               description = "Returns hourly energy consumption data for charts (line/bar)")
    public ResponseEntity<List<EnergyConsumption>> getUserEnergyConsumptionByDate(
            @Parameter(description = "UUID of the user") @PathVariable UUID userId,
            @Parameter(description = "Date in format yyyy-MM-dd") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<EnergyConsumption> data = energyConsumptionService.getUserEnergyConsumptionByDate(userId, date);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get device's energy consumption for a specific date")
    public ResponseEntity<List<EnergyConsumption>> getDeviceEnergyConsumptionByDate(
            @Parameter(description = "UUID of the device") @PathVariable UUID deviceId,
            @Parameter(description = "Date in format yyyy-MM-dd") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<EnergyConsumption> data = energyConsumptionService.getDeviceEnergyConsumptionByDate(deviceId, date);
        return ResponseEntity.ok(data);
    }
}
