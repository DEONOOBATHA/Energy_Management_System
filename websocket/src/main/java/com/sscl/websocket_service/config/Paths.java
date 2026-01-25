package com.sscl.websocket_service.config;

public class Paths {
    public static final String NOTIFICATION = "/api/notifications";
    
    // Notification (overconsumption alerts)
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String ROUTING_KEY = "notify.#";
    
    // Chat messages
    public static final String CHAT_QUEUE = "chat.queue";
    public static final String CHAT_EXCHANGE = "chat.exchange";
    public static final String CHAT_ROUTING_KEY = "chat.message";
    
    // WebSocket endpoints
    public static final String END_POINT = "/ws";
    public static final String TOPIC = "/topic";
    public static final String QUEUE = "/queue";
    public static final String APP = "/app";
    public static final String TOPIC_NOTIFICATIONS = "/topic/notifications";
    public static final String TOPIC_CHAT = "/topic/chat";
    
    public static final String CUSTOMER_CLINT_PORT = "http://localhost:4201";
    public static final String BANK_CLINT_PORT = "http://localhost:4200";
}
