package com.example.demo.controller;

import com.example.demo.dto.UserEventDTO;
import com.example.demo.entity.Person;
import com.example.demo.entity.Role;
import com.example.demo.publisher.UserEventPublisher;
import com.example.demo.repository.PersonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
@RequestMapping("/auth/users")
public class PersonController {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;
    private final SecureRandom secureRandom = new SecureRandom();

    public PersonController(PersonRepository personRepository, 
                          PasswordEncoder passwordEncoder,
                          UserEventPublisher userEventPublisher) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEventPublisher = userEventPublisher;
    }

    private String generateRandomPassword() {
        byte[] randomBytes = new byte[12];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@RequestBody Map<String, Object> userData) {
        String id = (String) userData.get("id");
        String name = (String) userData.get("name");
        String address = (String) userData.get("address");
        Integer age = userData.get("age") != null ? (Integer) userData.get("age") : null;
        
        // Check if username already exists
        if (personRepository.findByUsername(name).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Username already exists",
                "username", name
            ));
        }
        
        String randomPassword = generateRandomPassword();
        
        Person person = new Person();
        UUID userId;
        if (id != null) {
            userId = UUID.fromString(id);
            person.setId(userId);
        } else {
            userId = UUID.randomUUID();
            person.setId(userId);
        }
        person.setUsername(name);
        person.setPassword(passwordEncoder.encode(randomPassword));
        person.setRoles(Set.of(Role.CLIENT));
        personRepository.save(person);
        
        // Publish user created event to RabbitMQ
        UserEventDTO event = new UserEventDTO("CREATED", userId, name, address, age);
        userEventPublisher.publishUserCreated(event);
        
        // Return the password so admin can see it
        return ResponseEntity.ok(Map.of(
            "username", name,
            "password", randomPassword
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable UUID id, @RequestBody Map<String, Object> userData) {
        return personRepository.findById(id)
            .map(person -> {
                String name = (String) userData.get("name");
                String address = (String) userData.get("address");
                Integer age = userData.get("age") != null ? (Integer) userData.get("age") : null;
                
                person.setUsername(name);
                personRepository.save(person);
                
                // Publish user updated event to RabbitMQ
                UserEventDTO event = new UserEventDTO("UPDATED", id, name, address, age);
                userEventPublisher.publishUserUpdated(event);
                
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        return personRepository.findById(id)
            .map(person -> {
                // Clear roles before deleting to avoid foreign key issues
                person.getRoles().clear();
                personRepository.save(person);
                personRepository.delete(person);
                
                // Publish user deleted event to RabbitMQ
                UserEventDTO event = new UserEventDTO("DELETED", id, null, null, null);
                userEventPublisher.publishUserDeleted(event);
                
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
