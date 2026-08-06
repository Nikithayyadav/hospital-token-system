package com.hospital.token.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("NORMAL")
public class NormalToken extends Token {

    public NormalToken() {
        super();
    }

    public NormalToken(
            Patient patient,
            Doctor doctor,
            Department department,
            String reasonForVisit) {

        super(
                patient,
                doctor,
                department,
                TokenType.NORMAL,
                reasonForVisit
        );
    }

    @Override
    public int getQueuePriority() {
        return 100;
    }
}