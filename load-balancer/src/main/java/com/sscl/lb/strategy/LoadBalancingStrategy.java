package com.sscl.lb.strategy;

/**
 * Load balancing strategy interface
 */
public interface LoadBalancingStrategy {
    /**
     * Select a replica index based on the message/key
     * @param replicaCount total number of replicas
     * @param key load balancing key (e.g., deviceId)
     * @return replica index (1-based)
     */
    int selectReplica(int replicaCount, String key);
}
