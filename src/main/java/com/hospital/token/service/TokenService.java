package com.hospital.token.service;

import com.hospital.token.interfaces.TokenOperations;
import com.hospital.token.model.*;
import com.hospital.token.repository.TokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TokenService implements TokenOperations {

    private final TokenRepository tokenRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public TokenService(
            TokenRepository tokenRepository,
            PatientService patientService,
            DoctorService doctorService) {

        this.tokenRepository = tokenRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }


    // ================================
    // NORMAL TOKEN
    // ================================

    @Override
    @Transactional
    public Token generateNormalToken(
            Long patientId,
            Long doctorId,
            String reasonForVisit) {

        Patient patient =
                patientService.getPatient(patientId);

        Doctor doctor =
                doctorService.getDoctor(doctorId);

        if (patient == null) {
            throw new IllegalArgumentException(
                    "Patient not found."
            );
        }

        if (!patient.isActive()) {
            throw new IllegalStateException(
                    "Patient record is inactive."
            );
        }

        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Doctor not found."
            );
        }
        if (!doctor.isActive()) {

            throw new IllegalStateException(
                    "Selected doctor is inactive."
            );
        }

        if (doctor.getStatus() == DoctorStatus.OFF_DUTY
                || doctor.getStatus() == DoctorStatus.UNAVAILABLE) {

            throw new IllegalStateException(
                    "Selected doctor cannot currently receive an emergency patient."
            );
        }

        if (!doctor.canAcceptPatient()) {
            throw new IllegalStateException(
                    "Selected doctor is currently unavailable or the daily token limit has been reached."
            );
        }

        NormalToken token =
                new NormalToken(
                        patient,
                        doctor,
                        doctor.getDepartment(),
                        reasonForVisit
                );

        Token savedToken =
                tokenRepository.save(token);

        String tokenNumber =
                generateTokenNumber(
                        savedToken,
                        TokenType.NORMAL
                );

        savedToken.setTokenNumber(tokenNumber);

        savedToken =
                tokenRepository.save(savedToken);

        doctor.setTokensIssuedToday(
                doctor.getTokensIssuedToday() + 1
        );

        doctor.setCurrentWaitingCount(
                doctor.getCurrentWaitingCount() + 1
        );

        doctorService.saveDoctor(doctor);

        return savedToken;
    }


    // ================================
    // EMERGENCY TOKEN
    // ================================

    @Override
    @Transactional
    public Token generateEmergencyToken(
            Long patientId,
            Long doctorId,
            String reasonForVisit,
            String priority) {

        Patient patient =
                patientService.getPatient(patientId);

        Doctor doctor =
                doctorService.getDoctor(doctorId);

        if (patient == null) {
            throw new IllegalArgumentException(
                    "Patient not found."
            );
        }

        if (!patient.isActive()) {
            throw new IllegalStateException(
                    "Patient record is inactive."
            );
        }

        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Doctor not found."
            );
        }

        EmergencyPriority emergencyPriority;

        try {

            emergencyPriority =
                    EmergencyPriority.valueOf(
                            priority.toUpperCase()
                    );

        } catch (Exception exception) {

            throw new IllegalArgumentException(
                    "Invalid emergency priority."
            );
        }

        EmergencyToken token =
                new EmergencyToken(
                        patient,
                        doctor,
                        doctor.getDepartment(),
                        reasonForVisit,
                        emergencyPriority
                );

        Token savedToken =
                tokenRepository.save(token);

        savedToken.setTokenNumber(
                generateTokenNumber(
                        savedToken,
                        TokenType.EMERGENCY
                )
        );

        savedToken =
                tokenRepository.save(savedToken);

        doctor.setTokensIssuedToday(
                doctor.getTokensIssuedToday() + 1
        );

        doctor.setCurrentWaitingCount(
                doctor.getCurrentWaitingCount() + 1
        );

        doctorService.saveDoctor(doctor);

        return savedToken;
    }


    // ================================
    // TOKEN NUMBER
    // ================================

    private String generateTokenNumber(
            Token token,
            TokenType type) {

        String prefix;

        switch (type) {

            case EMERGENCY:
                prefix = "E";
                break;

            case NORMAL:
            default:
                prefix = "A";
                break;
        }

        return prefix
                + "-"
                + String.format(
                "%03d",
                token.getId()
        );
    }


    // ================================
    // WAITING QUEUE
    // ================================

    public List<Token> getWaitingQueue() {

        List<Token> databaseTokens =
                tokenRepository
                        .findByTokenDateAndTokenTypeAndStatusOrderByGeneratedAtAsc(
                                LocalDate.now(),
                                TokenType.NORMAL,
                                TokenStatus.WAITING
                        );

        /*
         * Required Java concepts:
         *
         * Queue
         * LinkedList
         */

        Queue<Token> queue =
                new LinkedList<>();

        for (Token token : databaseTokens) {
            queue.offer(token);
        }

        return new ArrayList<>(queue);
    }


    // ================================
    // EMERGENCY QUEUE
    // ================================

    public List<Token> getEmergencyQueue() {

        List<Token> emergencyTokens =
                tokenRepository
                        .findByTokenDateAndTokenTypeAndStatusOrderByGeneratedAtAsc(
                                LocalDate.now(),
                                TokenType.EMERGENCY,
                                TokenStatus.WAITING
                        );

        PriorityQueue<Token> priorityQueue =
                new PriorityQueue<>(
                        Comparator
                                .comparingInt(
                                        Token::getQueuePriority
                                )
                                .thenComparing(
                                        Token::getGeneratedAt
                                )
                );

        priorityQueue.addAll(emergencyTokens);

        List<Token> orderedTokens =
                new ArrayList<>();

        while (!priorityQueue.isEmpty()) {

            orderedTokens.add(
                    priorityQueue.poll()
            );
        }

        return orderedTokens;
    }


    // ================================
    // PATIENT HISTORY
    // ================================

    public List<Token> getPatientHistory(
            Long patientId) {

        return tokenRepository
                .findByPatientIdOrderByGeneratedAtDesc(
                        patientId
                );
    }


    // ================================
    // COUNTS
    // ================================

    public long getWaitingCount() {

        return tokenRepository
                .countByTokenDateAndTokenTypeAndStatus(
                        LocalDate.now(),
                        TokenType.NORMAL,
                        TokenStatus.WAITING
                );
    }


    public long getEmergencyCount() {

        return tokenRepository
                .countByTokenDateAndTokenTypeAndStatus(
                        LocalDate.now(),
                        TokenType.EMERGENCY,
                        TokenStatus.WAITING
                );
    }


    // ================================
    // CANCEL TOKEN
    // ================================

    @Override
    @Transactional
    public boolean cancelToken(Long tokenId) {

        Token token =
                tokenRepository
                        .findById(tokenId)
                        .orElse(null);

        if (token == null) {
            return false;
        }

        if (token.getStatus()
                == TokenStatus.COMPLETED) {

            return false;
        }

        TokenStatus oldStatus =
                token.getStatus();

        token.setStatus(
                TokenStatus.CANCELLED
        );

        tokenRepository.save(token);

        if (oldStatus == TokenStatus.WAITING) {

            decreaseDoctorWaitingCount(
                    token.getDoctor()
            );
        }

        // If token was in consultation, free the doctor
        if (oldStatus == TokenStatus.IN_CONSULTATION) {

            doctorService.updateDoctorStatus(
                    token.getDoctor().getId(),
                    DoctorStatus.AVAILABLE
            );
        }

        return true;
    }


    // ================================
    // CALL NEXT PATIENT
    // ================================

    @Override
    @Transactional
    public Token callNextPatient(Long doctorId) {

        List<Token> waitingTokens =
                tokenRepository
                        .findByDoctorIdAndTokenDateAndStatusOrderByGeneratedAtAsc(
                                doctorId,
                                LocalDate.now(),
                                TokenStatus.WAITING
                        );

        if (waitingTokens.isEmpty()) {
            return null;
        }

        /*
         * Polymorphism:
         * Token reference can contain either
         * NormalToken or EmergencyToken.
         */

        Token nextToken =
                selectNextToken(waitingTokens);

        nextToken.setStatus(
                TokenStatus.CALLED
        );

        nextToken.setCalledAt(
                LocalDateTime.now()
        );

        tokenRepository.save(nextToken);

        decreaseDoctorWaitingCount(
                nextToken.getDoctor()
        );

        return nextToken;
    }

        @Override
        @Transactional
        public boolean startConsultation(Long tokenId) {

                Token token = tokenRepository.findById(tokenId).orElse(null);

                if (token == null) {
                        return false;
                }

                if (token.getStatus() == TokenStatus.IN_CONSULTATION
                                || token.getStatus() == TokenStatus.COMPLETED) {

                        return false;
                }

                token.setStatus(TokenStatus.IN_CONSULTATION);

                tokenRepository.save(token);

                // mark doctor as in consultation
                doctorService.updateDoctorStatus(
                                token.getDoctor().getId(),
                                DoctorStatus.IN_CONSULTATION
                );

                return true;
        }


    private Token selectNextToken(
            List<Token> tokens) {

        PriorityQueue<Token> queue =
                new PriorityQueue<>(
                        Comparator
                                .comparingInt(
                                        Token::getQueuePriority
                                )
                                .thenComparing(
                                        Token::getGeneratedAt
                                )
                );

        queue.addAll(tokens);

        return queue.poll();
    }


    // ================================
    // COMPLETE TOKEN
    // ================================

    @Override
    @Transactional
    public boolean completeToken(
            Long tokenId) {

        Token token =
                tokenRepository
                        .findById(tokenId)
                        .orElse(null);

        if (token == null) {
            return false;
        }

        if (token.getStatus()
                == TokenStatus.COMPLETED) {

            return false;
        }

        TokenStatus previousStatus =
                token.getStatus();

        token.setStatus(
                TokenStatus.COMPLETED
        );

        token.setCompletedAt(
                LocalDateTime.now()
        );

        tokenRepository.save(token);

        if (previousStatus
                == TokenStatus.WAITING) {

            decreaseDoctorWaitingCount(
                    token.getDoctor()
            );
        }

        // If completion follows an active consultation,
        // set doctor to AVAILABLE
        if (previousStatus == TokenStatus.IN_CONSULTATION) {

            doctorService.updateDoctorStatus(
                    token.getDoctor().getId(),
                    DoctorStatus.AVAILABLE
            );
        }

        return true;
    }


    private void decreaseDoctorWaitingCount(
            Doctor doctor) {

        if (doctor == null) {
            return;
        }

        int waiting =
                doctor.getCurrentWaitingCount();

        if (waiting > 0) {

            doctor.setCurrentWaitingCount(
                    waiting - 1
            );

            doctorService.saveDoctor(
                    doctor
            );
        }
    }
    public Token getToken(Long id) {

        return tokenRepository
                .findById(id)
                .orElse(null);
    }

        // Return the currently called or in-consultation token for a doctor (if any)
        public Token getCurrentTokenForDoctor(Long doctorId) {

                List<Token> tokens = tokenRepository.findByDoctorIdAndTokenDateAndStatusOrderByGeneratedAtAsc(
                                doctorId,
                                LocalDate.now(),
                                TokenStatus.CALLED
                );

                if (!tokens.isEmpty()) {
                        return tokens.get(0);
                }

                tokens = tokenRepository.findByDoctorIdAndTokenDateAndStatusOrderByGeneratedAtAsc(
                                doctorId,
                                LocalDate.now(),
                                TokenStatus.IN_CONSULTATION
                );

                if (!tokens.isEmpty()) {
                        return tokens.get(0);
                }

                return null;
        }

        // Peek the next waiting token for a doctor without changing state
        public Token getNextWaitingForDoctor(Long doctorId) {

                List<Token> waitingTokens = tokenRepository.findByDoctorIdAndTokenDateAndStatusOrderByGeneratedAtAsc(
                                doctorId,
                                LocalDate.now(),
                                TokenStatus.WAITING
                );

                if (waitingTokens.isEmpty()) {
                        return null;
                }

                return selectNextToken(waitingTokens);
        }
}