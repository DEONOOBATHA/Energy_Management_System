package com.sscl.websocket_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host:localhost}")
    private String syncBrokerHost;

    @Value("${spring.rabbitmq.port:5672}")
    private int syncBrokerPort;

    @Value("${spring.rabbitmq.username:root}")
    private String syncBrokerUsername;

    @Value("${spring.rabbitmq.password:root}")
    private String syncBrokerPassword;

    @Bean
    @Primary
    public ConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(syncBrokerHost);
        factory.setPort(syncBrokerPort);
        factory.setUsername(syncBrokerUsername);
        factory.setPassword(syncBrokerPassword);
        return factory;
    }

    // Notification queue and exchange (for overconsumption alerts)
    @Bean
    Queue notificationQueue() {
        return new Queue(Paths.NOTIFICATION_QUEUE, true);
    }

    @Bean
    TopicExchange notificationExchange() {
        return new TopicExchange(Paths.NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(Paths.ROUTING_KEY);
    }

    // Chat queue and exchange (for chat messages)
    @Bean
    Queue chatQueue() {
        return new Queue(Paths.CHAT_QUEUE, true);
    }

    @Bean
    TopicExchange chatExchange() {
        return new TopicExchange(Paths.CHAT_EXCHANGE, true, false);
    }

    @Bean
    Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(chatQueue).to(chatExchange).with(Paths.CHAT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory rabbitConnectionFactory) {
        RabbitTemplate template = new RabbitTemplate(rabbitConnectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory rabbitConnectionFactory) {
        return new RabbitAdmin(rabbitConnectionFactory);
    }
}
