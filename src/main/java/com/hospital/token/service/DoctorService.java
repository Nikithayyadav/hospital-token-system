package com.hospital.token.service;

import com.hospital.token.model.Doctor;
import com.hospital.token.model.DoctorStatus;
import com.hospital.token.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(
            DoctorRepository doctorRepository) {

        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> getAllDoctors() {

        return new ArrayList<>(
                doctorRepository.findByActiveTrue()
        );
    }

    public Doctor getDoctor(Long id) {

        return doctorRepository
                .findById(id)
                .orElse(null);
    }

    public List<Doctor> getDoctorsByDepartment(
            Long departmentId) {

        return new ArrayList<>(
                doctorRepository
                        .findByDepartmentIdAndActiveTrue(
                                departmentId
                        )
        );
    }

    public List<Doctor> getAvailableDoctors() {

        return new ArrayList<>(
                doctorRepository
                        .findByStatusAndActiveTrue(
                                DoctorStatus.AVAILABLE
                        )
        );
    }

    public boolean isDoctorAvailable(Long doctorId) {

        Doctor doctor = getDoctor(doctorId);

        if (doctor == null) {
            return false;
        }

        return doctor.canAcceptPatient();
    }

    public Doctor updateDoctorStatus(
            Long doctorId,
            DoctorStatus status) {

        Doctor doctor = getDoctor(doctorId);

        if (doctor == null) {
            return null;
        }

        doctor.setStatus(status);

        return doctorRepository.save(doctor);
    }

    public long getAvailableDoctorCount() {

        return doctorRepository
                .countByStatusAndActiveTrue(
                        DoctorStatus.AVAILABLE
                );
    }
    public Doctor saveDoctor(Doctor doctor) {

        return doctorRepository.save(doctor);
    }
}