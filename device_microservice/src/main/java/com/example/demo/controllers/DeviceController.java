package com.example.demo.controllers;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.services.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@Validated
@Tag(name = "Device Management", description = "Operations for managing IoT devices in the system")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get all devices", description = "Retrieves a list of all IoT devices registered in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of devices")
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        return ResponseEntity.ok(deviceService.findDevices());
    }

    @PostMapping
    @Operation(summary = "Create new device", description = "Registers a new IoT device with specified details including description, address, and maximum hourly energy consumption")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Device created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Void>create(@Valid @RequestBody DeviceDetailsDTO details) {
        UUID id = deviceService.insert(details);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID", description = "Retrieves detailed information about a specific device by its UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Device found"),
        @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<DeviceDetailsDTO> getDevice(
            @Parameter(description = "UUID of the device to retrieve") @PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update device", description = "Updates an existing device's information including description, address, and energy consumption parameters")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Device updated successfully"),
        @ApiResponse(responseCode = "404", description = "Device not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Void> updateDevice(
            @Parameter(description = "UUID of the device to update") @PathVariable UUID id, 
            @Valid @RequestBody DeviceDetailsDTO deviceDetailsDTO) {
        deviceService.update(id, deviceDetailsDTO);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete device", description = "Removes a device from the system by its UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Device deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "UUID of the device to delete") @PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.ok().build();
    }
}
