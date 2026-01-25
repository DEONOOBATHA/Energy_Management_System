package com.example.demo.consumer;

import com.example.demo.dto.UserEventDTO;
import com.example.demo.entities.Person;
import com.example.demo.repositories.PersonRepository;
import com.example.demo.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {

    private final PersonRepository personRepository;

    @RabbitListener(queues = RabbitMQConfig.DEVICE_USER_SYNC_QUEUE)
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
        Person person = new Person();
        person.setId(event.getUserId());
        person.setName(event.getName());
        personRepository.save(person);
        log.info("User synchronized in device service: {}", person.getId());
    }

    private void handleUserUpdated(UserEventDTO event) {
        personRepository.findById(event.getUserId()).ifPresent(person -> {
            person.setName(event.getName());
            personRepository.save(person);
            log.info("User updated in device service: {}", person.getId());
        });
    }

    private void handleUserDeleted(UserEventDTO event) {
        personRepository.deleteById(event.getUserId());
        log.info("User deleted from device service: {}", event.getUserId());
    }
}
