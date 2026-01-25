package com.example.demo.service.security.details;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.entity.Person;
import com.example.demo.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokenService;
    private final PersonRepository userRepository;

    public AuthResponse authenticate(AuthRequest credentials) {
        log.debug("Attempting authentication for user: {}", credentials.username());
        
        // Create authentication token from provided credentials
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(credentials.username(), credentials.password());
        
        try {
            // Perform authentication
            Authentication authResult = authenticationManager.authenticate(authToken);
            log.info("User authenticated successfully: {}", authResult.getName());
            
            // Generate JWT token for authenticated user
            String generatedToken = tokenService.generateToken(authResult);
            Long tokenExpiration = tokenService.extractExpirationTime(generatedToken);
            
            // Build and return authentication response
            return buildAuthResponse(generatedToken, authResult.getName(), tokenExpiration);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", credentials.username(), e);
            throw e;
        }
    }

    private AuthResponse buildAuthResponse(String token, String username, Long expiresAt) {
        return new AuthResponse(token, username, expiresAt);
    }

    public Person getUserByUsername(String username) {
        log.debug("Retrieving user from database: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found in database: {}", username);
                    return new RuntimeException("User with username '" + username + "' does not exist");
                });
    }
}