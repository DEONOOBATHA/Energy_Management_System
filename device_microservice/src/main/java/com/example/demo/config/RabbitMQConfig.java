package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String USER_EXCHANGE = "user.events";
    public static final String DEVICE_EXCHANGE = "device.events";
    public static final String ENERGY_EXCHANGE = "energy.events";

    // Routing keys
    public static final String USER_CREATED_KEY = "user.created";
    public static final String USER_UPDATED_KEY = "user.updated";
    public static final String USER_DELETED_KEY = "user.deleted";
    public static final String DEVICE_CREATED_KEY = "device.created";
    public static final String DEVICE_UPDATED_KEY = "device.updated";
    public static final String DEVICE_DELETED_KEY = "device.deleted";
    public static final String DEVICE_ASSOCIATION_DELETED_KEY = "device.association.deleted";
    public static final String ENERGY_HOURLY_KEY = "energy.hourly";

    // Queues
    public static final String DEVICE_USER_SYNC_QUEUE = "device.user.synch.queue";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public TopicExchange deviceExchange() {
        return new TopicExchange(DEVICE_EXCHANGE);
    }

    @Bean
    public TopicExchange energyExchange() {
        return new TopicExchange(ENERGY_EXCHANGE);
    }

    @Bean
    public Queue deviceUserSyncQueue() {
        return new Queue(DEVICE_USER_SYNC_QUEUE, true);
    }

    // Binding for consuming user events
    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder.bind(deviceUserSyncQueue())
                .to(userExchange())
                .with(USER_CREATED_KEY);
    }

    @Bean
    public Binding userUpdatedBinding() {
        return BindingBuilder.bind(deviceUserSyncQueue())
                .to(userExchange())
                .with(USER_UPDATED_KEY);
    }

    @Bean
    public Binding userDeletedBinding() {
        return BindingBuilder.bind(deviceUserSyncQueue())
                .to(userExchange())
                .with(USER_DELETED_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
