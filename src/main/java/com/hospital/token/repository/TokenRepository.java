package com.hospital.token.repository;

import com.hospital.token.model.Token;
import com.hospital.token.model.TokenStatus;
import com.hospital.token.model.TokenType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface TokenRepository
        extends JpaRepository<Token, Long> {


    // GET ALL TOKENS FOR A DATE
    List<Token> findByTokenDateOrderByGeneratedAtAsc(
            LocalDate tokenDate
    );


    // GET TOKENS BY DATE + STATUS
    List<Token> findByTokenDateAndStatusOrderByGeneratedAtAsc(
            LocalDate tokenDate,
            TokenStatus status
    );


    // GET DOCTOR TOKENS BY DATE + STATUS
    List<Token> findByDoctorIdAndTokenDateAndStatusOrderByGeneratedAtAsc(
            Long doctorId,
            LocalDate tokenDate,
            TokenStatus status
    );


    // GET TOKENS BY TYPE + STATUS
    List<Token> findByTokenDateAndTokenTypeAndStatusOrderByGeneratedAtAsc(
            LocalDate tokenDate,
            TokenType tokenType,
            TokenStatus status
    );


    // PATIENT TOKEN HISTORY
    List<Token> findByPatientIdOrderByGeneratedAtDesc(
            Long patientId
    );


    // COUNT ALL TOKENS FOR DATE
    long countByTokenDate(
            LocalDate tokenDate
    );


    // COUNT TOKENS BY STATUS
    long countByTokenDateAndStatus(
            LocalDate tokenDate,
            TokenStatus status
    );


    // COUNT TOKENS BY TYPE + STATUS
    long countByTokenDateAndTokenTypeAndStatus(
            LocalDate tokenDate,
            TokenType tokenType,
            TokenStatus status
    );


    // COUNT DOCTOR TOKENS
    long countByDoctorIdAndTokenDate(
            Long doctorId,
            LocalDate tokenDate
    );
}