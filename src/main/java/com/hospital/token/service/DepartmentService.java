package com.hospital.token.service;

import com.hospital.token.model.Department;
import com.hospital.token.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository) {

        this.departmentRepository = departmentRepository;
    }

    public List<Department> getActiveDepartments() {

        return new ArrayList<>(
                departmentRepository.findByActiveTrue()
        );
    }

    public Department getDepartment(Long id) {

        return departmentRepository
                .findById(id)
                .orElse(null);
    }
}