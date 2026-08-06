package com.hospital.token.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("EMERGENCY")
public class EmergencyToken extends Token {

    @Enumerated(EnumType.STRING)
    private EmergencyPriority emergencyPriority;

    public EmergencyToken() {
        super();
    }

    public EmergencyToken(
            Patient patient,
            Doctor doctor,
            Department department,
            String reasonForVisit,
            EmergencyPriority emergencyPriority) {

        super(
                patient,
                doctor,
                department,
                TokenType.EMERGENCY,
                reasonForVisit
        );

        this.emergencyPriority = emergencyPriority;
    }

    @Override
    public int getQueuePriority() {

        if (emergencyPriority == null) {
            return 3;
        }

        return emergencyPriority.getPriorityValue();
    }

    public EmergencyPriority getEmergencyPriority() {
        return emergencyPriority;
    }

    public void setEmergencyPriority(
            EmergencyPriority emergencyPriority) {

        this.emergencyPriority = emergencyPriority;
    }
}