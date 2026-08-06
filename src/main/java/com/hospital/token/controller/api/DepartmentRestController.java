package com.hospital.token.controller.api;

import com.hospital.token.model.Department;
import com.hospital.token.service.DepartmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@Tag(
        name = "Department Management",
        description = "APIs for hospital departments"
)
public class DepartmentRestController {

    private final DepartmentService departmentService;

    public DepartmentRestController(
            DepartmentService departmentService) {

        this.departmentService = departmentService;
    }


    @GetMapping
    @Operation(
            summary = "Get all active departments",
            description = "Returns all active departments available for consultation"
    )
    public ResponseEntity<List<Department>> getAllDepartments() {

        return ResponseEntity.ok(
                departmentService.getActiveDepartments()
        );
    }
}