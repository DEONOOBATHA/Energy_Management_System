package com.example.monitoring.publisher;

import com.example.monitoring.config.RabbitMQConfig;
import com.example.monitoring.dto.OverConsumptionAlertDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OverConsumptionPublisher {
    private final RabbitTemplate rabbitTemplate;

    public OverConsumptionPublisher(@Qualifier("syncRabbitTemplate") RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOverConsumptionAlert(OverConsumptionAlertDTO alertDTO) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                alertDTO
        );
        log.info("Published OverConsumptionAlert to sync broker: exchange='{}', routing='{}'", 
                RabbitMQConfig.NOTIFICATION_EXCHANGE, RabbitMQConfig.ROUTING_KEY);
    }
}
