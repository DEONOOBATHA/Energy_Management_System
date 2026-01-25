package com.sscl.chat.dto;

import com.sscl.chat.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private ChatMessage.ResponseSource responseSource;
    private String timestamp;  // Changed to String for JSON serialization
    private String sessionId;
    private String messageType;  // USER_TO_ADMIN or ADMIN_TO_USER
}
