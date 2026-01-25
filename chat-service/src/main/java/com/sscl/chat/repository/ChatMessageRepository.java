package com.sscl.chat.repository;

import com.sscl.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByUserIdOrderByTimestampDesc(UUID userId);
    
    List<ChatMessage> findByUserIdOrderByTimestampAsc(UUID userId);

    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);

    List<ChatMessage> findTop50ByUserIdOrderByTimestampDesc(UUID userId);
    
    List<ChatMessage> findTop50ByUserIdOrderByTimestampAsc(UUID userId);

    List<ChatMessage> findByTimestampAfterOrderByTimestampDesc(LocalDateTime timestamp);
}
