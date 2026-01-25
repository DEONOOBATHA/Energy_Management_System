package com.sscl.chat.publisher;

import com.sscl.chat.config.RabbitMQConfig;
import com.sscl.chat.dto.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish chat response to RabbitMQ for WebSocket delivery
     */
    public void publishChatResponse(ChatResponseDto response) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CHAT_EXCHANGE,
                    RabbitMQConfig.CHAT_ROUTING_KEY,
                    response
            );
            log.info("Published chat response to RabbitMQ: userId={}, messageId={}, source={}",
                    response.getUserId(), response.getMessageId(), response.getResponseSource());
        } catch (Exception e) {
            log.error("Failed to publish chat response to RabbitMQ", e);
        }
    }
}
