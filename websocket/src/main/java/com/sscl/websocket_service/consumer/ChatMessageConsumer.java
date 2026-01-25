package com.sscl.websocket_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sscl.websocket_service.config.Paths;
import com.sscl.websocket_service.dto.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = Paths.CHAT_QUEUE)
    public void consumeChatMessage(ChatResponseDto chatResponse) {
        try {
            log.info("📨 Received from RabbitMQ: userId={}, messageId={}, hasResponse={}",
                    chatResponse.getUserId(), chatResponse.getMessageId(), 
                    chatResponse.getResponse() != null);

            String messageJson = objectMapper.writeValueAsString(chatResponse);
            
            // ALWAYS send to user's topic
            String userTopic = Paths.TOPIC_CHAT + "/" + chatResponse.getUserId();
            messagingTemplate.convertAndSend(userTopic, messageJson);
            log.info("✅ Message sent to user topic: {}", userTopic);
            
            // ALWAYS send to admin topic
            String adminTopic = Paths.TOPIC_CHAT + "/admin";
            messagingTemplate.convertAndSend(adminTopic, messageJson);
            log.info("✅ Message sent to admin topic: {}", adminTopic);

        } catch (Exception e) {
            log.error("❌ Error processing chat message from RabbitMQ", e);
        }
    }
}