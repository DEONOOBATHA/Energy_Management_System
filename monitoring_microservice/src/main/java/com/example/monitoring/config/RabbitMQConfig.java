package com.example.monitoring.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.amqp.core.Declarables;

@Configuration
public class RabbitMQConfig {

    // Sync Broker configuration (for user/device synchronization)
    @Value("${spring.rabbitmq.host}")
    private String syncBrokerHost;
    
    @Value("${spring.rabbitmq.port}")
    private int syncBrokerPort;
    
    @Value("${spring.rabbitmq.username}")
    private String syncBrokerUsername;
    
    @Value("${spring.rabbitmq.password}")
    private String syncBrokerPassword;

    // Data Collection Broker configuration (for energy consumption)
    @Value("${data.collection.rabbitmq.host}")
    private String dataCollectionBrokerHost;
    
    @Value("${data.collection.rabbitmq.port}")
    private int dataCollectionBrokerPort;
    
    @Value("${data.collection.rabbitmq.username}")
    private String dataCollectionBrokerUsername;
    
    @Value("${data.collection.rabbitmq.password}")
    private String dataCollectionBrokerPassword;

    // Exchange names
    public static final String USER_EXCHANGE = "user.events";
    public static final String DEVICE_EXCHANGE = "device.events";
    public static final String ENERGY_EXCHANGE = "energy.events";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
   

    // Queue names (each consumer has its own queue) - FORCED REBUILD
    public static final String USER_SYNC_QUEUE = "user.synch.queue";
    public static final String DEVICE_SYNC_QUEUE = "device.synch.queue";
    public static final String ENERGY_CONSUMPTION_QUEUE = "energy.cons.queue";

    // Routing keys
    public static final String USER_CREATED_KEY = "user.created";
    public static final String USER_UPDATED_KEY = "user.updated";
    public static final String USER_DELETED_KEY = "user.deleted";
    public static final String DEVICE_CREATED_KEY = "device.created";
    public static final String DEVICE_UPDATED_KEY = "device.updated";
    public static final String DEVICE_DELETED_KEY = "device.deleted";
    public static final String ENERGY_HOURLY_KEY = "energy.hourly";
    public static final String ROUTING_KEY = "notify.#";

    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange(NOTIFICATION_EXCHANGE).durable(true).build();
    }
    

    // Connection Factories
    @Bean
    @Primary
    @Qualifier("syncConnectionFactory")
    public ConnectionFactory syncConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(syncBrokerHost);
        factory.setPort(syncBrokerPort);
        factory.setUsername(syncBrokerUsername);
        factory.setPassword(syncBrokerPassword);
        return factory;
    }

    @Bean
    @Qualifier("dataCollectionConnectionFactory")
    public ConnectionFactory dataCollectionConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(dataCollectionBrokerHost);
        factory.setPort(dataCollectionBrokerPort);
        factory.setUsername(dataCollectionBrokerUsername);
        factory.setPassword(dataCollectionBrokerPassword);
        return factory;
    }

    // Message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate for Sync Broker
    @Bean
    @Primary
    @Qualifier("syncRabbitTemplate")
    public RabbitTemplate syncRabbitTemplate(@Qualifier("syncConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // RabbitTemplate for Data Collection Broker
    @Bean
    @Qualifier("dataCollectionRabbitTemplate")
    public RabbitTemplate dataCollectionRabbitTemplate(@Qualifier("dataCollectionConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // RabbitAdmin for Sync Broker
    @Bean
    @Primary
    public RabbitAdmin syncRabbitAdmin(@Qualifier("syncConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // RabbitAdmin for Data Collection Broker
    @Bean
    public RabbitAdmin dataCollectionRabbitAdmin(@Qualifier("dataCollectionConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // Listener Container Factory for Sync Broker (user/device consumers)
    @Bean
    @Primary
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            @Qualifier("syncConnectionFactory") ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }


    // Listener Container Factory for Data Collection Broker (energy consumer)
    @Bean
    public SimpleRabbitListenerContainerFactory dataCollectionRabbitListenerContainerFactory(
            @Qualifier("dataCollectionConnectionFactory") ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
    @Value("${replica.id:1}")
    private int replicaId;

    /**
     * Declare the replica-specific ingest queue for load-balanced consumption.
     * Queue name format: monitoring.ingest.1, monitoring.ingest.2, monitoring.ingest.3
     */
    @Bean
    public Queue loadBalancedIngestQueue() {
        String queueName = "monitoring.ingest." + replicaId;
        return new Queue(queueName, true, false, false);
    }

    /**
     * Bind queue to dataCollectionRabbitAdmin so it gets declared on the data collection broker
     */
    @Bean
    public Declarables dataCollectionDeclarables() {
        return new Declarables(loadBalancedIngestQueue());
    }

}
