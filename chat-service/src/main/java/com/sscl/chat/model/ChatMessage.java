package com.sscl.chat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseSource responseSource;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "session_id")
    private String sessionId;

    public enum MessageType {
        USER_TO_ADMIN,
        ADMIN_TO_USER,
        SYSTEM
    }

    public enum ResponseSource {
        RULE_BASED,
        AI_GENERATED,
        ADMIN,
        ADMIN_MANUAL,
        SYSTEM
    }
}
