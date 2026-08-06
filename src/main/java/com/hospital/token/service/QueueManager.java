package com.hospital.token.service;

import com.hospital.token.model.EmergencyToken;
import com.hospital.token.model.NormalToken;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

@Component
public class QueueManager {

    /*
     * Normal patients:
     * First In First Out
     */
    private final Queue<NormalToken> normalQueue =
            new LinkedList<>();


    /*
     * Emergency patients:
     * Ordered according to emergency priority.
     */
    private final PriorityQueue<EmergencyToken> emergencyQueue =
            new PriorityQueue<>(
                    Comparator
                            .comparingInt(
                                    EmergencyToken::getQueuePriority
                            )
                            .thenComparing(
                                    EmergencyToken::getGeneratedAt
                            )
            );


    public void addNormalToken(NormalToken token) {

        normalQueue.offer(token);
    }


    public void addEmergencyToken(EmergencyToken token) {

        emergencyQueue.offer(token);
    }


    public NormalToken getNextNormalPatient() {

        return normalQueue.poll();
    }


    public EmergencyToken getNextEmergencyPatient() {

        return emergencyQueue.poll();
    }


    public NormalToken viewNextNormalPatient() {

        return normalQueue.peek();
    }


    public EmergencyToken viewNextEmergencyPatient() {

        return emergencyQueue.peek();
    }


    public int getNormalQueueSize() {

        return normalQueue.size();
    }


    public int getEmergencyQueueSize() {

        return emergencyQueue.size();
    }


    public Queue<NormalToken> getNormalQueue() {

        return new LinkedList<>(normalQueue);
    }


    public PriorityQueue<EmergencyToken> getEmergencyQueue() {

        return new PriorityQueue<>(emergencyQueue);
    }
}