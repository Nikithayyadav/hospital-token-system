package com.hospital.token.repository;

import com.hospital.token.model.Doctor;
import com.hospital.token.model.DoctorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository
        extends JpaRepository<Doctor, Long> {

    List<Doctor> findByActiveTrue();

    List<Doctor> findByDepartmentIdAndActiveTrue(Long departmentId);

    List<Doctor> findByStatusAndActiveTrue(DoctorStatus status);

    long countByStatusAndActiveTrue(DoctorStatus status);
}