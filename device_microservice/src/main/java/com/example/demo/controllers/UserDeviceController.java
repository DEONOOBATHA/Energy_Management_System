package com.example.demo.controllers;

import com.example.demo.dtos.UserDeviceDTO;
import com.example.demo.services.UserDeviceService;
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
@RequestMapping("/user_devices")
@Validated
@Tag(name = "User-Device Association", description = "Operations for managing associations between users and their IoT devices")
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    public UserDeviceController( UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @GetMapping
    @Operation(summary = "Get all user-device associations", description = "Retrieves list of all user-device association records")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<UserDeviceDTO>> getUserDevices() {
        return ResponseEntity.ok(userDeviceService.findUserDevices());
    }

    @PostMapping
    @Operation(summary = "Create user-device association", description = "Associates a device with a user by creating a new mapping record")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Association created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data or association already exists")
    })
    public ResponseEntity<Void> create(@Valid @RequestBody UserDeviceDTO userDeviceDTO) {
        Long id = userDeviceService.insert(userDeviceDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get association by ID", description = "Retrieves a specific user-device association by its internal ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Association found"),
        @ApiResponse(responseCode = "404", description = "Association not found")
    })
    public ResponseEntity<UserDeviceDTO> getUserDevicesbyId(
            @Parameter(description = "Internal ID of the association") @PathVariable Long id) {
        return ResponseEntity.ok(userDeviceService.findUserDeviceById(id));
    }

    @GetMapping("/by_device/{deviceid}")
    @Operation(summary = "Get association by device ID", description = "Retrieves user-device association for a specific device UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Association found"),
        @ApiResponse(responseCode = "404", description = "No association found for this device")
    })
    public ResponseEntity<UserDeviceDTO> getUserDevicesbyDeviceId(
            @Parameter(description = "UUID of the device") @PathVariable UUID deviceid) {
        return ResponseEntity.ok(userDeviceService.findUserDeviceByDeviceId(deviceid));
    }

    @GetMapping("/by_user/{userid}")
    @Operation(summary = "Get associations by user ID", description = "Retrieves all devices associated with a specific user UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found or has no devices")
    })
    public ResponseEntity<List<UserDeviceDTO>> getUserDevicesbyUserId(
            @Parameter(description = "UUID of the user") @PathVariable UUID userid) {
        return ResponseEntity.ok(userDeviceService.findUserDevicesByUserId(userid));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update association", description = "Updates an existing user-device association record")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Association updated successfully"),
        @ApiResponse(responseCode = "404", description = "Association not found"),
        @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    public ResponseEntity<Void> updateUserDevice(
            @Parameter(description = "Internal ID of the association") @PathVariable Long id, 
            @Valid @RequestBody UserDeviceDTO userDeviceDTO) {
        userDeviceService.update(id, userDeviceDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete association", description = "Removes a user-device association, unassigning the device from the user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Association deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Association not found")
    })
    public ResponseEntity<Void> deleteUserDevice(
            @Parameter(description = "Internal ID of the association") @PathVariable Long id) {
        userDeviceService.delete(id);
        return ResponseEntity.ok().build();
    }
}
