package com.hospital.token.repository;

import com.hospital.token.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    List<Department> findByActiveTrue();
}