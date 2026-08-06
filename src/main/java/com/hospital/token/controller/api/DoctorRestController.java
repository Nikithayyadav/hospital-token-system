package com.hospital.token.controller.api;

import com.hospital.token.dto.response.ApiResponse;
import com.hospital.token.model.Doctor;
import com.hospital.token.service.DoctorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.hospital.token.model.DoctorStatus;

@RestController
@RequestMapping("/api/doctors")
@Tag(
        name = "Doctor Management",
        description = "APIs for doctor information and availability"
)
public class DoctorRestController {

    private final DoctorService doctorService;

    public DoctorRestController(
            DoctorService doctorService) {

        this.doctorService = doctorService;
    }


    // ==========================================
    // GET DOCTORS
    // ==========================================

    @GetMapping
    @Operation(
            summary = "Get doctors",
            description = """
                    Returns doctor information.

                    Examples:

                    /api/doctors

                    /api/doctors?departmentId=2

                    /api/doctors?available=true

                    /api/doctors?departmentId=2&available=true
                    """
    )
    public ResponseEntity<ApiResponse<?>>
    getDoctors(

            @RequestParam(required = false)
            Long departmentId,

            @RequestParam(required = false)
            Boolean available) {

        List<Doctor> doctors;

        // Department + Available
        if (departmentId != null &&
                Boolean.TRUE.equals(available)) {

            doctors = doctorService
                    .getDoctorsByDepartment(departmentId);

            doctors.removeIf(
                    doctor -> doctor.getStatus() != DoctorStatus.AVAILABLE
            );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            doctors,
                            Map.of(
                                    "count", doctors.size(),
                                    "departmentId", departmentId,
                                    "available", true
                            )
                    )
            );
        }

        // Department only
        if (departmentId != null) {

            doctors = doctorService
                    .getDoctorsByDepartment(departmentId);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            doctors,
                            Map.of(
                                    "count", doctors.size(),
                                    "departmentId", departmentId
                            )
                    )
            );
        }

        // Available only
        if (Boolean.TRUE.equals(available)) {

            doctors = doctorService
                    .getAvailableDoctors();

            return ResponseEntity.ok(
                    ApiResponse.success(
                            doctors,
                            Map.of(
                                    "count", doctors.size(),
                                    "available", true
                            )
                    )
            );
        }

        // All Doctors
        doctors = doctorService.getAllDoctors();

        return ResponseEntity.ok(
                ApiResponse.success(
                        doctors,
                        Map.of(
                                "count", doctors.size()
                        )
                )
        );
    }


    // ==========================================
    // GET DOCTOR BY ID
    // ==========================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Get doctor by ID",
            description = "Returns a doctor's complete information using the database ID."
    )
    public ResponseEntity<ApiResponse<?>> getDoctor(
            @PathVariable Long id) {

        Doctor doctor =
                doctorService.getDoctor(id);

        if (doctor == null) {

            return ResponseEntity
                    .status(404)
                    .body(
                            ApiResponse.error(
                                    "Doctor not found"
                            )
                    );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        doctor
                )
        );
    }
}