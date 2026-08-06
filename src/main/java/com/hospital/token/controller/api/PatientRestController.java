package com.hospital.token.controller.api;

import com.hospital.token.dto.PatientRequest;
import com.hospital.token.dto.response.ApiResponse;
import com.hospital.token.model.Patient;
import com.hospital.token.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/patients")
@Tag(
        name = "Patient Management",
        description = "APIs for patient registration, search, sorting, update and deletion"
)
public class PatientRestController {

    private final PatientService patientService;


    public PatientRestController(
            PatientService patientService) {

        this.patientService = patientService;
    }


    // ==========================================
    // GET / SEARCH / SORT PATIENTS
    // ==========================================

    @GetMapping
    @Operation(
            summary = "Get, search or sort patients",
            description = """
                    Returns all active patients.

                    Optional query parameters:

                    search - Search using Patient ID, name or phone number.

                    sort=name - Sort patients alphabetically.

                    sort=patientId - Sort patients by generated Patient ID.

                    Examples:
                    /api/patients
                    /api/patients?search=Ravi
                    /api/patients?sort=name
                    /api/patients?sort=patientId
                    """
    )
    public ResponseEntity<ApiResponse<?>> getPatients(

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String sort) {


        // ======================================
        // SEARCH PATIENTS
        // ======================================

        if (search != null &&
                !search.trim().isEmpty()) {

            List<Patient> patients =
                    patientService.searchPatients(
                            search.trim()
                    );

            return ResponseEntity.ok(
                    ApiResponse.success(
                            patients,
                            Map.of(
                                    "count", patients.size(),
                                    "search", search.trim()
                            )
                    )
            );
        }


        // ======================================
        // SORT BY NAME
        // ======================================

        if ("name".equalsIgnoreCase(sort)) {

            Set<Patient> patients =
                    patientService
                            .getPatientsAlphabetically();

            return ResponseEntity.ok(
                    ApiResponse.success(
                            patients,
                            Map.of(
                                    "count", patients.size(),
                                    "sort", "name"
                            )
                    )
            );
        }


        // ======================================
        // SORT BY PATIENT ID
        // ======================================

        if ("patientId".equalsIgnoreCase(sort)) {

            Map<String, Patient> patients =
                    patientService
                            .getPatientsById();

            return ResponseEntity.ok(
                    ApiResponse.success(
                            patients,
                            Map.of(
                                    "count", patients.size(),
                                    "sort", "patientId"
                            )
                    )
            );
        }


        // ======================================
        // INVALID SORT VALUE
        // ======================================

        if (sort != null &&
                !sort.trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            ApiResponse.error(
                                    "Invalid sort value. Use name or patientId"
                            )
                    );
        }


        // ======================================
        // GET ALL ACTIVE PATIENTS
        // ======================================

        List<Patient> patients =
                patientService.getAllPatients();


        return ResponseEntity.ok(
                ApiResponse.success(
                        patients,
                        Map.of(
                                "count",
                                patients.size()
                        )
                )
        );
    }


    // ==========================================
    // REGISTER PATIENT
    // ==========================================

    @PostMapping
    @Operation(
            summary = "Register patient",
            description = """
                    Registers a new patient.

                    The database ID, hospital Patient ID,
                    active status and registration time
                    are generated automatically by the system.
                    """
    )
    public ResponseEntity<ApiResponse<Patient>>
    registerPatient(
            @RequestBody PatientRequest request) {

        try {

            Patient patient =
                    new Patient();


            patient.setName(
                    request.getName()
            );

            patient.setAge(
                    request.getAge()
            );

            patient.setGender(
                    request.getGender()
            );

            patient.setPhone(
                    request.getPhone()
            );

            patient.setAddress(
                    request.getAddress()
            );

            patient.setBloodGroup(
                    request.getBloodGroup()
            );


            Patient savedPatient =
                    patientService
                            .registerPatient(
                                    patient
                            );


            return ResponseEntity.ok(
                    ApiResponse.success(
                            savedPatient
                    )
            );


        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            ApiResponse.error(
                                    exception.getMessage()
                            )
                    );
        }
    }


    // ==========================================
    // UPDATE PATIENT
    // ==========================================

    @PutMapping("/{id}")
    @Operation(
            summary = "Update patient",
            description = """
                    Updates an existing patient's
                    demographic and contact information.

                    System-generated fields such as
                    Patient ID and registration time
                    are not modified.
                    """
    )
    public ResponseEntity<ApiResponse<Patient>>
    updatePatient(
            @PathVariable Long id,
            @RequestBody PatientRequest request) {


        Patient patient =
                new Patient();


        patient.setName(
                request.getName()
        );

        patient.setAge(
                request.getAge()
        );

        patient.setGender(
                request.getGender()
        );

        patient.setPhone(
                request.getPhone()
        );

        patient.setAddress(
                request.getAddress()
        );

        patient.setBloodGroup(
                request.getBloodGroup()
        );


        Patient updatedPatient =
                patientService
                        .updatePatient(
                                id,
                                patient
                        );


        if (updatedPatient == null) {

            return ResponseEntity
                    .status(404)
                    .body(
                            ApiResponse.error(
                                    "Patient not found"
                            )
                    );
        }


        return ResponseEntity.ok(
                ApiResponse.success(
                        updatedPatient
                )
        );
    }


    // ==========================================
    // DELETE PATIENT
    // ==========================================

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete patient",
            description = """
                    Soft deletes the selected patient.

                    The patient record is marked inactive
                    instead of being permanently removed.
                    Active consultation tokens associated
                    with the patient are also cancelled.
                    """
    )
    public ResponseEntity<ApiResponse<String>>
    deletePatient(
            @PathVariable Long id) {


        boolean deleted =
                patientService
                        .deletePatient(id);


        if (!deleted) {

            return ResponseEntity
                    .status(404)
                    .body(
                            ApiResponse.error(
                                    "Patient not found or already inactive"
                            )
                    );
        }


        return ResponseEntity.ok(
                ApiResponse.success(
                        "Patient deleted successfully"
                )
        );
    }
}