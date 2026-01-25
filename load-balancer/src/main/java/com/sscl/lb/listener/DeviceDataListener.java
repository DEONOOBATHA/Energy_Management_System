package com.sscl.lb.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sscl.lb.service.LoadBalancerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeviceDataListener {

    private final LoadBalancerService loadBalancerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeviceDataListener(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    /**
     * Consume device data from central queue and distribute to replicas
     */
    @RabbitListener(queues = "${load-balancer.queue.device-data}")
    public void processDeviceData(String message) {
        try {
            log.debug("Received device data message: {}", message);

            // Extract device ID from message (assuming JSON format)
            JsonNode jsonNode = objectMapper.readTree(message);
            String deviceId = jsonNode.has("deviceId") ?
                    jsonNode.get("deviceId").asText() : "unknown";

            // Route to appropriate replica
            loadBalancerService.routeDeviceData(message, deviceId);

        } catch (Exception e) {
            log.error("Error processing device data: {}", message, e);
            // Re-throw to trigger message retry/DLQ
            throw new RuntimeException("Device data processing failed", e);
        }
    }
}
