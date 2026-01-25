package com.sscl.websocket_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
    private UUID messageId;
    private UUID userId;
    private String username;
    private String message;
    private String response;
    private String responseSource;
    private String timestamp; // String for WebSocket compatibility
    private String sessionId;
    private String messageType;  // USER_TO_ADMIN or ADMIN_TO_USER
}
