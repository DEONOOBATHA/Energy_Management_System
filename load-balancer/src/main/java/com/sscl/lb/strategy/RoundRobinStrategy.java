package com.sscl.lb.strategy;

import java.util.Random;

/**
 * Round-Robin Load Balancing Strategy
 * Distributes messages evenly across replicas in sequence
 */
public class RoundRobinStrategy implements LoadBalancingStrategy {
    private int currentIndex = 0;
    private final Object lock = new Object();

    @Override
    public int selectReplica(int replicaCount, String key) {
        synchronized (lock) {
            int selected = (currentIndex % replicaCount) + 1;
            currentIndex++;
            return selected;
        }
    }
}
