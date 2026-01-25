package com.sscl.lb.service;

import com.sscl.lb.strategy.ConsistentHashingStrategy;
import com.sscl.lb.strategy.LoadBalancingStrategy;
import com.sscl.lb.strategy.RoundRobinStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LoadBalancerService {

    private final RabbitTemplate rabbitTemplate;
    private final int replicaCount;
    private final LoadBalancingStrategy strategy;
    private final String ingestQueuePrefix;

    public LoadBalancerService(RabbitTemplate rabbitTemplate,
                              @Value("${load-balancer.replicas}") int replicaCount,
                              @Value("${load-balancer.strategy}") String strategyName,
                              @Value("${load-balancer.queue.monitoring-ingest-prefix}") String ingestPrefix) {
        this.rabbitTemplate = rabbitTemplate;
        this.replicaCount = replicaCount;
        this.ingestQueuePrefix = ingestPrefix;

        // Select strategy
        if ("round-robin".equalsIgnoreCase(strategyName)) {
            this.strategy = new RoundRobinStrategy();
        } else {
            this.strategy = new ConsistentHashingStrategy(); // default
        }

        log.info("Load Balancer initialized with {} replicas using {} strategy",
                replicaCount, strategyName);
    }

    /**
     * Route a device data message to appropriate replica queue
     * @param message device data message
     * @param deviceId device identifier for consistent hashing
     */
    public void routeDeviceData(String message, String deviceId) {
        try {
            // Select replica using strategy
            int replicaIndex = strategy.selectReplica(replicaCount, deviceId);

            // Construct target queue name
            String targetQueue = ingestQueuePrefix + replicaIndex;

            // Send to replica-specific ingest queue
            rabbitTemplate.convertAndSend(targetQueue, message);

            log.debug("Routed device data from device {} to replica {} (queue: {})",
                    deviceId, replicaIndex, targetQueue);

        } catch (Exception e) {
            log.error("Error routing device data for device {}", deviceId, e);
            throw new RuntimeException("Load balancing failed", e);
        }
    }

    /**
     * Get load distribution metrics
     */
    public Map<String, Object> getMetrics() {
        return new HashMap<>() {{
            put("replicas", replicaCount);
            put("strategy", strategy.getClass().getSimpleName());
            put("ingestQueuePrefix", ingestQueuePrefix);
        }};
    }
}
