package com.example.demo.publisher;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.UserEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserCreated(UserEventDTO userEvent) {
        log.info("Publishing user created event for user: {}", userEvent.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_CREATED_KEY,
                userEvent
        );
    }

    public void publishUserUpdated(UserEventDTO userEvent) {
        log.info("Publishing user updated event for user: {}", userEvent.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_UPDATED_KEY,
                userEvent
        );
    }

    public void publishUserDeleted(UserEventDTO userEvent) {
        log.info("Publishing user deleted event for user: {}", userEvent.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_DELETED_KEY,
                userEvent
        );
    }
}
