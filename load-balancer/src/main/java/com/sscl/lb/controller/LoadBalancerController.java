package com.sscl.lb.controller;

import com.sscl.lb.service.LoadBalancerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/load-balancer")
public class LoadBalancerController {

    private final LoadBalancerService loadBalancerService;

    public LoadBalancerController(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    /**
     * Get load balancer metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(loadBalancerService.getMetrics());
    }
}
