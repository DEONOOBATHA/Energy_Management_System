package com.sscl.lb.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${load-balancer.replicas}")
    private int replicas;

    @Value("${load-balancer.queue.device-data}")
    private String deviceDataQueue;

    @Value("${load-balancer.queue.monitoring-ingest-prefix}")
    private String ingestPrefix;

    /**
     * Device data queue - consumed by load balancer
     */
    @Bean
    public Queue deviceDataQueue() {
        return new Queue(deviceDataQueue, true, false, false);
    }

    /**
     * Per-replica ingest queues
     */
    @Bean
    public Queue[] replicaQueues() {
        Queue[] queues = new Queue[replicas];
        for (int i = 1; i <= replicas; i++) {
            queues[i - 1] = new Queue(ingestPrefix + i, true, false, false);
        }
        return queues;
    }
}
