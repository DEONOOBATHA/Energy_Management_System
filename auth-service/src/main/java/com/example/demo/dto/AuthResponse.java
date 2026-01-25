package com.example.demo.dto;

public record AuthResponse(
        String jwtToken,
        String username,
        Long expiresAt
) {
}
