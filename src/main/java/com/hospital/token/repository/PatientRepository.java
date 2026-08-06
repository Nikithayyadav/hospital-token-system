package com.hospital.token.repository;

import com.hospital.token.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository
        extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientId(String patientId);

    List<Patient> findByNameContainingIgnoreCase(String name);

    List<Patient> findByPhoneContaining(String phone);

    List<Patient> findByActiveTrue();

    long countByActiveTrue();
}