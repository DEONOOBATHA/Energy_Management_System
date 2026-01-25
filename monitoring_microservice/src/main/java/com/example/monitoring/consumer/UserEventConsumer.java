package com.example.monitoring.consumer;

import com.example.monitoring.config.RabbitMQConfig;
import com.example.monitoring.dto.UserEventDTO;
import com.example.monitoring.entity.Person;
import com.example.monitoring.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {

    private final PersonRepository personRepository;

    @RabbitListener(
        containerFactory = "rabbitListenerContainerFactory",
        bindings = {
            @QueueBinding(
                value = @Queue(name = RabbitMQConfig.USER_SYNC_QUEUE, durable = "true"),
                exchange = @Exchange(name = RabbitMQConfig.USER_EXCHANGE, type = "topic"),
                key = {"user.created", "user.updated", "user.deleted"}
            )
        }
    )
    public void handleUserEvent(UserEventDTO event) {
        log.info("Received user event: {} for user ID: {}", event.getEventType(), event.getUserId());

        try {
            switch (event.getEventType()) {
                case "CREATED":
                    handleUserCreated(event);
                    break;
                case "UPDATED":
                    handleUserUpdated(event);
                    break;
                case "DELETED":
                    handleUserDeleted(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing user event: {}", e.getMessage(), e);
        }
    }

    private void handleUserCreated(UserEventDTO event) {
        Person person = Person.builder()
                .id(event.getUserId())
                .name(event.getName())
                .address(event.getAddress())
                .age(event.getAge())
                .build();
        
        personRepository.save(person);
        log.info("User synchronized: {}", person.getId());
    }

    private void handleUserUpdated(UserEventDTO event) {
        personRepository.findById(event.getUserId()).ifPresent(person -> {
            person.setName(event.getName());
            person.setAddress(event.getAddress());
            person.setAge(event.getAge());
            personRepository.save(person);
            log.info("User updated: {}", person.getId());
        });
    }

    private void handleUserDeleted(UserEventDTO event) {
        personRepository.deleteById(event.getUserId());
        log.info("User deleted: {}", event.getUserId());
    }
}
