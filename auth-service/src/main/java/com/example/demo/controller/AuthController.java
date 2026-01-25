package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.service.security.details.AuthService;
import com.example.demo.service.security.details.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public AuthResponse auth(@RequestBody AuthRequest authRequest) {
        return authService.authenticate(authRequest);
    }
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.info("=== JWT Validation Started ===");
        log.info("Authorization header received: {}", authHeader != null ? "Bearer ***" : "null");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Invalid or missing Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        try {
            String token = authHeader.substring(7);
            log.info("Token extracted, length: {}", token.length());
            log.debug("Token content: {}", token);
            
            log.info("Attempting to decode token...");
            Jwt decodedJwt = jwtTokenService.decodeToken(token);
            log.info("Token decoded successfully");

            String userId = decodedJwt.getSubject();
            String scopes = decodedJwt.getClaimAsString("scope");
            String issuer = decodedJwt.getIssuer() != null ? decodedJwt.getIssuer().toString() : null;
            
            log.info("Token claims - Subject: {}, Issuer: {}, Scopes: {}", userId, issuer, scopes);

            if (issuer == null || !issuer.equals("http://localhost:8080")) {
                log.warn("Invalid token issuer. Expected: 'http://localhost:8080', Got: '{}'", issuer);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token issuer");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-User-ID", userId != null ? userId : "unknown");
            headers.add("X-User-Scopes", scopes != null ? scopes : "none");
            
            log.info("Token validation successful. User: {}, Scopes: {}", userId, scopes);
            log.info("=== JWT Validation Completed Successfully ===");

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .build(); // Return empty body with headers for forward auth

        } catch (JwtValidationException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            log.error("JWT validation exception details: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalid: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage());
            log.error("Exception details: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token validation error: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        try {
            String token = authHeader.substring(7);
            Jwt decodedJwt = jwtTokenService.decodeToken(token);
            String username = decodedJwt.getSubject();
            
            // Get user from database by username
            var user = authService.getUserByUsername(username);
            
            return ResponseEntity.ok(java.util.Map.of(
                "id", user.getId().toString(),
                "username", user.getUsername(),
                "roles", user.getRoles()
            ));
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

}
