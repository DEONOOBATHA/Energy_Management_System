package com.sscl.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMessageDto {
    private UUID userId;          // Target user
    private String message;       // Admin's message
    private String adminUsername; // Admin identifier
    private String sessionId;     // Chat session
}
