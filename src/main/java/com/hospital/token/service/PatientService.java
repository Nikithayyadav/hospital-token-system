package com.hospital.token.service;

import com.hospital.token.model.Patient;
import com.hospital.token.model.Token;
import com.hospital.token.model.TokenStatus;

import com.hospital.token.repository.PatientRepository;
import com.hospital.token.repository.TokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final TokenRepository tokenRepository;


    public PatientService(
            PatientRepository patientRepository,
            TokenRepository tokenRepository) {

        this.patientRepository = patientRepository;
        this.tokenRepository = tokenRepository;
    }


    // ==========================================
    // REGISTER PATIENT
    // ==========================================

    public Patient registerPatient(
            Patient patient) {

        patient.setActive(true);


        Patient savedPatient =
                patientRepository.save(patient);


        String generatedPatientId =
                String.format(
                        "P%05d",
                        savedPatient.getId()
                );


        savedPatient.setPatientId(
                generatedPatientId
        );


        return patientRepository.save(
                savedPatient
        );
    }


    // ==========================================
    // GET ALL ACTIVE PATIENTS
    // ==========================================

    public List<Patient> getAllPatients() {

        return new ArrayList<>(
                patientRepository.findByActiveTrue()
        );
    }


    // ==========================================
    // SEARCH BY PATIENT ID
    // ==========================================

    public Patient findByPatientId(
            String patientId) {


        Optional<Patient> patient =
                patientRepository
                        .findByPatientId(
                                patientId
                        );


        return patient.orElse(null);
    }


    // ==========================================
    // SEARCH PATIENTS
    // ==========================================

    public List<Patient> searchPatients(
            String keyword) {


        List<Patient> results =
                new ArrayList<>();


        if (keyword == null ||
                keyword.trim().isEmpty()) {

            return getAllPatients();
        }


        String searchKeyword =
                keyword.trim();


        // SEARCH BY PATIENT ID
        Patient patientById =
                findByPatientId(
                        searchKeyword
                );


        if (patientById != null &&
                patientById.isActive()) {

            results.add(
                    patientById
            );
        }


        // SEARCH BY NAME
        List<Patient> nameResults =
                patientRepository
                        .findByNameContainingIgnoreCase(
                                searchKeyword
                        );


        // SEARCH BY PHONE
        List<Patient> phoneResults =
                patientRepository
                        .findByPhoneContaining(
                                searchKeyword
                        );


        // ADD ONLY ACTIVE PATIENTS
        for (Patient patient : nameResults) {

            if (patient.isActive()) {
                results.add(patient);
            }
        }


        for (Patient patient : phoneResults) {

            if (patient.isActive()) {
                results.add(patient);
            }
        }


        // REMOVE DUPLICATES
        Set<Patient> uniquePatients =
                new HashSet<>(
                        results
                );


        return new ArrayList<>(
                uniquePatients
        );
    }


    // ==========================================
    // ALPHABETICAL ORDER USING TREESET
    // ==========================================

    public Set<Patient>
    getPatientsAlphabetically() {


        TreeSet<Patient> patients =
                new TreeSet<>();


        patients.addAll(
                patientRepository
                        .findByActiveTrue()
        );


        return patients;
    }


    // ==========================================
    // PATIENTS SORTED BY PATIENT ID
    // USING TREEMAP
    // ==========================================

    public Map<String, Patient>
    getPatientsById() {


        TreeMap<String, Patient> patientMap =
                new TreeMap<>();


        List<Patient> patients =
                patientRepository
                        .findByActiveTrue();


        for (Patient patient : patients) {

            patientMap.put(
                    patient.getPatientId(),
                    patient
            );
        }


        return patientMap;
    }


    // ==========================================
    // GET PATIENT BY DATABASE ID
    // ==========================================

    public Patient getPatient(
            Long id) {


        return patientRepository
                .findById(id)
                .orElse(null);
    }


    // ==========================================
    // UPDATE PATIENT
    // ==========================================

    public Patient updatePatient(
            Long id,
            Patient updatedPatient) {


        Patient existing =
                getPatient(id);


        if (existing == null ||
                !existing.isActive()) {

            return null;
        }


        existing.setName(
                updatedPatient.getName()
        );

        existing.setAge(
                updatedPatient.getAge()
        );

        existing.setGender(
                updatedPatient.getGender()
        );

        existing.setPhone(
                updatedPatient.getPhone()
        );

        existing.setAddress(
                updatedPatient.getAddress()
        );

        existing.setBloodGroup(
                updatedPatient.getBloodGroup()
        );


        return patientRepository.save(
                existing
        );
    }


    // ==========================================
    // SOFT DELETE PATIENT
    // ==========================================

    @Transactional
    public boolean deletePatient(
            Long id) {


        Patient patient =
                getPatient(id);


        if (patient == null ||
                !patient.isActive()) {

            return false;
        }


        // ------------------------------------------
        // FIND ALL TOKENS BELONGING TO THIS PATIENT
        // ------------------------------------------

        List<Token> patientTokens =
                tokenRepository
                        .findByPatientIdOrderByGeneratedAtDesc(
                                id
                        );


        // ------------------------------------------
        // CANCEL ACTIVE TOKENS
        // ------------------------------------------

        for (Token token : patientTokens) {


            TokenStatus status =
                    token.getStatus();


            if (status == TokenStatus.WAITING ||
                    status == TokenStatus.CALLED ||
                    status == TokenStatus.IN_CONSULTATION) {


                token.setStatus(
                        TokenStatus.CANCELLED
                );


                tokenRepository.save(
                        token
                );
            }
        }


        // ------------------------------------------
        // SOFT DELETE PATIENT
        // ------------------------------------------

        patient.setActive(false);


        patientRepository.save(
                patient
        );


        return true;
    }


    // ==========================================
    // TOTAL ACTIVE PATIENT COUNT
    // ==========================================

    public long getTotalPatientCount() {

        return patientRepository
                .countByActiveTrue();
    }
}