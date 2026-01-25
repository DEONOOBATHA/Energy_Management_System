package com.example.demo.controllers;

import com.example.demo.dtos.PersonDTO;
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
@RequestMapping("/persons")
@Validated
@Tag(name = "Person Sync", description = "Operations for synchronizing user data in device microservice")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    @Operation(summary = "Get all persons", description = "Retrieves list of all users synchronized from user microservice")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<PersonDTO>> getPeople() {
        return ResponseEntity.ok(personService.findPersons());
    }

    @PostMapping
    @Operation(summary = "Create person record", description = "Creates a synchronized copy of user data in device microservice")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Person record created"),
        @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    public ResponseEntity<Void> create(@Valid @RequestBody PersonDTO person) {
        UUID id = personService.insert(person);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get person by ID", description = "Retrieves a specific person record by UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Person found"),
        @ApiResponse(responseCode = "404", description = "Person not found")
    })
    public ResponseEntity<PersonDTO> getPerson(
            @Parameter(description = "UUID of the person") @PathVariable UUID id) {
        return ResponseEntity.ok(personService.findPersonById(id));
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update person", description = "Updates synchronized person data in device microservice")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Person updated"),
        @ApiResponse(responseCode = "404", description = "Person not found")
    })
    public ResponseEntity<Void> updatePerson(
            @Parameter(description = "UUID of the person") @PathVariable UUID id, 
            @Valid @RequestBody PersonDTO person) {
        personService.update(id, person);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete person", description = "Removes person record from device microservice")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Person deleted"),
        @ApiResponse(responseCode = "404", description = "Person not found")
    })
    public ResponseEntity<Void> deletePerson(
            @Parameter(description = "UUID of the person") @PathVariable UUID id) {
        personService.delete(id);
        return ResponseEntity.ok().build();
    }
}
