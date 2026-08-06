package com.hospital.token.model;

public enum EmergencyPriority {

    CRITICAL(1),
    HIGH(2),
    MODERATE(3);

    private final int priorityValue;

    EmergencyPriority(int priorityValue) {
        this.priorityValue = priorityValue;
    }

    public int getPriorityValue() {
        return priorityValue;
    }
}