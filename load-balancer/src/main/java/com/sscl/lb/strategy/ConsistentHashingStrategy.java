package com.sscl.lb.strategy;

/**
 * Consistent Hashing Load Balancing Strategy
 * Uses hash of the key to deterministically select a replica
 */
public class ConsistentHashingStrategy implements LoadBalancingStrategy {

    @Override
    public int selectReplica(int replicaCount, String key) {
        if (key == null || key.isEmpty()) {
            key = "default";
        }

        // Get hash code and ensure positive result
        int hash = Math.abs(key.hashCode());

        // Map to replica index (1-based)
        int replicaIndex = (hash % replicaCount) + 1;
        return replicaIndex;
    }
}
