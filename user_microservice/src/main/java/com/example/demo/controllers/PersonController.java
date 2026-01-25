package com.example.demo.controllers;

import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.services.PersonService;
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
@CrossOrigin(origins = "*")
@RequestMapping("/people")
@Validated
@Tag(name = "Person Management", description = "Operations for managing users in the user microservice")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all users with their basic information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users")
    public ResponseEntity<List<PersonDTO>> getPeople() {
        return ResponseEntity.ok(personService.findPersons());
    }

    @PostMapping
    @Operation(summary = "Create new user", description = "Creates a new user with provided details including name, address, and age")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Void> create(@Valid @RequestBody PersonDetailsDTO person) {
        UUID id = personService.insert(person);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves detailed information about a specific user by their UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<PersonDetailsDTO> getPerson(
            @Parameter(description = "UUID of the user to retrieve") @PathVariable UUID id) {
        return ResponseEntity.ok(personService.findPersonById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user's information. Address and age are optional fields")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Void> updatePerson(
            @Parameter(description = "UUID of the user to update") @PathVariable UUID id, 
            @Valid @RequestBody PersonDetailsDTO person) {
        personService.update(id, person);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user from the system by their UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deletePerson(
            @Parameter(description = "UUID of the user to delete") @PathVariable UUID id) {
        personService.delete(id);
        return ResponseEntity.ok().build();
    }
}
