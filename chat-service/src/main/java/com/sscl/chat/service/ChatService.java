package com.sscl.chat.service;

import com.sscl.chat.dto.ChatMessageDto;
import com.sscl.chat.dto.ChatResponseDto;
import com.sscl.chat.model.ChatMessage;
import com.sscl.chat.publisher.ChatMessagePublisher;
import com.sscl.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Chat Service - Orchestrates rule-based and AI-driven responses
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final RuleBasedEngine ruleBasedEngine;
    private final AIService aiService;
    private final ChatMessagePublisher chatMessagePublisher;

    @Value("${chat.rule-based-enabled:true}")
    private boolean ruleBasedEnabled;

    @Value("${chat.ai-enabled:true}")
    private boolean aiEnabled;

    /**
     * Process incoming chat message and generate response
     */
    @Transactional
    public ChatResponseDto processMessage(ChatMessageDto messageDto) {
        log.info("Processing chat message from user: {} - '{}'", 
                 messageDto.getUsername(), messageDto.getMessage());

        String response;
        ChatMessage.ResponseSource responseSource;

        // Step 1: Try rule-based engine first
        if (ruleBasedEnabled) {
            response = ruleBasedEngine.processMessage(messageDto.getMessage());
            if (!response.equals("Your message has been sent to an AI entity. They will respond shortly")) {
                responseSource = ChatMessage.ResponseSource.RULE_BASED;
                log.info("Rule-based engine matched for user: {}", messageDto.getUsername());
            } else {
                // Step 2: Fall back to AI if no rule matched
                if (aiEnabled) {
                    log.info("⚡ CALLING aiService.generateResponse() for user: {}", messageDto.getUsername());
                    response = aiService.generateResponse(
                            messageDto.getMessage(), 
                            messageDto.getUsername()
                    );
                    responseSource = ChatMessage.ResponseSource.AI_GENERATED;
                    log.info("AI service generated response for user: {}", messageDto.getUsername());
                } else {
                    response = "I apologize, but I'm having trouble processing your request at the moment. " +
                               "An administrator has been notified and will respond to your inquiry shortly. " +
                               "Thank you for your patience!";
                    responseSource = ChatMessage.ResponseSource.SYSTEM;
                }
            }
        } else {
            // Only AI if rules disabled
            if (aiEnabled) {
                response = aiService.generateResponse(
                        messageDto.getMessage(), 
                        messageDto.getUsername()
                );
                responseSource = ChatMessage.ResponseSource.AI_GENERATED;
            } else {
                response = "I apologize, but I'm having trouble processing your request at the moment. " +
                           "An administrator has been notified and will respond to your inquiry shortly. " +
                           "Thank you for your patience!";
                responseSource = ChatMessage.ResponseSource.SYSTEM;
            }
        }

        // Step 3: Save conversation to database
        String username = messageDto.getUsername() != null 
                ? messageDto.getUsername() 
                : "User-" + messageDto.getUserId().toString().substring(0, 8);
        
        ChatMessage chatMessage = ChatMessage.builder()
                .userId(messageDto.getUserId())
                .username(username)
                .message(messageDto.getMessage())
                .response(response)
                .messageType(ChatMessage.MessageType.USER_TO_ADMIN)
                .responseSource(responseSource)
                .timestamp(LocalDateTime.now())
                .sessionId(messageDto.getSessionId())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.info("Chat message saved with ID: {}", savedMessage.getId());

        // Step 4: Build response DTO
        ChatResponseDto responseDto = ChatResponseDto.builder()
                .messageId(savedMessage.getId())
                .userId(savedMessage.getUserId())
                .username(savedMessage.getUsername())
                .message(savedMessage.getMessage())
                .response(savedMessage.getResponse())
                .responseSource(savedMessage.getResponseSource())
                .timestamp(savedMessage.getTimestamp().toString())  // Convert to String
                .sessionId(savedMessage.getSessionId())
                .messageType(ChatMessage.MessageType.USER_TO_ADMIN.toString())
                .build();

        // Step 5: Publish to RabbitMQ
        // Publishing strategy:
        // 1. User message → for admin dashboard (admin/topic)
        // 2. Response message → for both user and admin
        chatMessagePublisher.publishChatResponse(responseDto);
        log.info("Published chat response (message + response) to RabbitMQ");

        return responseDto;
    }

    /**
     * Get chat history for a user
     */
    public List<ChatResponseDto> getChatHistory(UUID userId, int limit) {
        log.info("Fetching chat history for user: {}, limit: {}", userId, limit);
        
        // Get latest 50 messages in DESC order, then reverse to ASC for UI
        List<ChatMessage> messages = limit > 0 
                ? chatMessageRepository.findTop50ByUserIdOrderByTimestampDesc(userId)
                : chatMessageRepository.findByUserIdOrderByTimestampAsc(userId);

        // Reverse DESC to ASC so newest messages appear at bottom
        if (limit > 0) {
            messages = new ArrayList<>(messages);
            java.util.Collections.reverse(messages);
        }

        return messages.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get chat history by session
     */
    public List<ChatResponseDto> getChatHistoryBySession(String sessionId) {
        log.info("Fetching chat history for session: {}", sessionId);
        
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);

        return messages.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ChatResponseDto mapToDto(ChatMessage message) {
        return ChatResponseDto.builder()
                .messageId(message.getId())
                .userId(message.getUserId())
                .username(message.getUsername())
                .message(message.getMessage())
                .response(message.getResponse())
                .responseSource(message.getResponseSource())
                .messageType(message.getMessageType() != null ? message.getMessageType().toString() : null)
                .timestamp(message.getTimestamp().toString())
                .sessionId(message.getSessionId())
                .build();
    }

    private String getDefaultResponse() {
        return "Thank you for your message. An administrator will review your inquiry " +
               "and respond to you shortly.";
    }

    /**
     * Process admin message to user (direct chat)
     */
    @Transactional
    public ChatResponseDto processAdminMessage(com.sscl.chat.dto.AdminMessageDto messageDto) {
        log.info("Processing admin message to user: {}", messageDto.getUserId());

        // Save admin message
        ChatMessage chatMessage = ChatMessage.builder()
                .userId(messageDto.getUserId())
                .username("Admin: " + messageDto.getAdminUsername())
                .message(messageDto.getMessage())
                .response("")  // Empty response for admin messages
                .messageType(ChatMessage.MessageType.ADMIN_TO_USER)
                .responseSource(ChatMessage.ResponseSource.ADMIN)
                .timestamp(LocalDateTime.now())
                .sessionId(messageDto.getSessionId())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.info("Admin message saved with ID: {}", savedMessage.getId());

        // Build response DTO
        ChatResponseDto responseDto = ChatResponseDto.builder()
                .messageId(savedMessage.getId())
                .userId(savedMessage.getUserId())
                .username(savedMessage.getUsername())
                .message(savedMessage.getMessage())
                .response("")
                .responseSource(ChatMessage.ResponseSource.ADMIN)
                .timestamp(savedMessage.getTimestamp().toString())
                .sessionId(savedMessage.getSessionId())
                .build();

        // Publish to RabbitMQ for WebSocket delivery to user
        chatMessagePublisher.publishChatResponse(responseDto);

        return responseDto;
    }

    /**
     * Get active chat sessions (for admin dashboard)
     */
    public List<ChatResponseDto> getActiveSessions() {
        // Get last messages from each unique session in last 24 hours
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        List<ChatMessage> recentMessages = chatMessageRepository
                .findByTimestampAfterOrderByTimestampDesc(yesterday);

        // Group by sessionId and get latest message per session
        Map<String, ChatMessage> sessionMap = new HashMap<>();
        for (ChatMessage msg : recentMessages) {
            if (msg.getSessionId() != null && !sessionMap.containsKey(msg.getSessionId())) {
                sessionMap.put(msg.getSessionId(), msg);
            }
        }

        return sessionMap.values().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}

