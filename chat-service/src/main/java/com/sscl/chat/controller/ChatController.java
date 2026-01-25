package com.sscl.chat.controller;

import com.sscl.chat.dto.AdminMessageDto;
import com.sscl.chat.dto.ChatMessageDto;
import com.sscl.chat.dto.ChatResponseDto;
import com.sscl.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    /**
     * Send a chat message and receive automated response
     */
    @PostMapping("/send")
    public ResponseEntity<ChatResponseDto> sendMessage(@RequestBody ChatMessageDto messageDto) {
        try {
            log.info("Received chat message from user: {} - '{}'", 
                     messageDto.getUsername(), messageDto.getMessage());

            ChatResponseDto response = chatService.processMessage(messageDto);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chat message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get chat history for a specific user
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatResponseDto>> getChatHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            log.info("Fetching chat history for user: {}", userId);
            List<ChatResponseDto> history = chatService.getChatHistory(userId, limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching chat history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get chat history for a specific session
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ChatResponseDto>> getChatHistoryBySession(@PathVariable String sessionId) {
        try {
            log.info("Fetching chat history for session: {}", sessionId);
            List<ChatResponseDto> history = chatService.getChatHistoryBySession(sessionId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching chat session history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Admin sends message to user (direct chat)
     */
    @PostMapping("/admin/send")
    public ResponseEntity<ChatResponseDto> adminSendMessage(@RequestBody AdminMessageDto messageDto) {
        try {
            log.info("Admin {} sending message to user: {}", 
                     messageDto.getAdminUsername(), messageDto.getUserId());

            ChatResponseDto response = chatService.processAdminMessage(messageDto);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing admin message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active chat sessions (for admin dashboard)
     */
    @GetMapping("/admin/sessions")
    public ResponseEntity<List<ChatResponseDto>> getActiveSessions() {
        try {
            log.info("Fetching active chat sessions");
            List<ChatResponseDto> sessions = chatService.getActiveSessions();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error fetching active sessions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chat Service is running");
    }
}
