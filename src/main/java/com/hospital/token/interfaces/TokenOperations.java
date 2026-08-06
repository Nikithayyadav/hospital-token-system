package com.hospital.token.interfaces;

import com.hospital.token.model.Token;

public interface TokenOperations {

    Token generateNormalToken(
            Long patientId,
            Long doctorId,
            String reasonForVisit
    );

    Token generateEmergencyToken(
            Long patientId,
            Long doctorId,
            String reasonForVisit,
            String priority
    );

    boolean cancelToken(Long tokenId);

    Token callNextPatient(Long doctorId);

    boolean completeToken(Long tokenId);
        boolean startConsultation(Long tokenId);
}