package com.hospital.token.controller.api;

import com.hospital.token.dto.TokenRequest;
import com.hospital.token.dto.TokenStatusRequest;
import com.hospital.token.dto.response.ApiResponse;

import com.hospital.token.model.Patient;
import com.hospital.token.model.Token;

import com.hospital.token.service.PatientService;
import com.hospital.token.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/tokens")
@Tag(
        name = "Token Management",
        description = "APIs for consultation token generation and hospital queue management"
)
public class TokenRestController {

    private final TokenService tokenService;
    private final PatientService patientService;


    public TokenRestController(
            TokenService tokenService,
            PatientService patientService) {

        this.tokenService = tokenService;
        this.patientService = patientService;
    }


    // ==========================================
    // GENERATE NORMAL / EMERGENCY TOKEN
    // ==========================================

    @PostMapping
    @Operation(
            summary = "Generate consultation token",
            description = """
                    Generates either a NORMAL or EMERGENCY consultation token.

                    patientId must be the hospital-generated Patient ID
                    such as P00009.

                    For NORMAL:
                    type = NORMAL
                    priority is not required.

                    For EMERGENCY:
                    type = EMERGENCY
                    priority must be provided.
                    """
    )
    public ResponseEntity<ApiResponse<?>>
    generateToken(
            @RequestBody TokenRequest request) {

        try {

            // ----------------------------------
            // VALIDATE PATIENT ID
            // ----------------------------------

            if (request.getPatientId() == null ||
                    request.getPatientId().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                ApiResponse.error(
                                        "Patient ID is required"
                                )
                        );
            }


            // ----------------------------------
            // FIND PATIENT USING HOSPITAL ID
            // ----------------------------------

            Patient patient =
                    patientService.findByPatientId(
                            request.getPatientId().trim()
                    );


            if (patient == null ||
                    !patient.isActive()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                ApiResponse.error(
                                        "Active patient not found for Patient ID: "
                                                + request.getPatientId()
                                )
                        );
            }


            // Internal database ID used by service
            Long databasePatientId =
                    patient.getId();


            // ----------------------------------
            // VALIDATE DOCTOR
            // ----------------------------------

            if (request.getDoctorId() == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                ApiResponse.error(
                                        "Doctor ID is required"
                                )
                        );
            }


            // ----------------------------------
            // VALIDATE TOKEN TYPE
            // ----------------------------------

            if (request.getType() == null ||
                    request.getType().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                ApiResponse.error(
                                        "Token type is required"
                                )
                        );
            }


            String type =
                    request.getType()
                            .trim()
                            .toUpperCase();


            Token token;


            // ==================================
            // EMERGENCY TOKEN
            // ==================================

            if ("EMERGENCY".equals(type)) {

                if (request.getPriority() == null ||
                        request.getPriority()
                                .trim()
                                .isEmpty()) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    ApiResponse.error(
                                            "Emergency priority is required"
                                    )
                            );
                }


                token =
                        tokenService
                                .generateEmergencyToken(
                                        databasePatientId,
                                        request.getDoctorId(),
                                        request.getReasonForVisit(),
                                        request.getPriority()
                                                .trim()
                                                .toUpperCase()
                                );
            }


            // ==================================
            // NORMAL TOKEN
            // ==================================

            else if ("NORMAL".equals(type)) {

                token =
                        tokenService
                                .generateNormalToken(
                                        databasePatientId,
                                        request.getDoctorId(),
                                        request.getReasonForVisit()
                                );
            }


            // ==================================
            // INVALID TYPE
            // ==================================

            else {

                return ResponseEntity
                        .badRequest()
                        .body(
                                ApiResponse.error(
                                        "Invalid token type. Use NORMAL or EMERGENCY"
                                )
                        );
            }


            return ResponseEntity.ok(
                    ApiResponse.success(
                            token,
                            Map.of(
                                    "patientId",
                                    patient.getPatientId(),

                                    "type",
                                    type
                            )
                    )
            );


        } catch (IllegalArgumentException |
                 IllegalStateException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            ApiResponse.error(
                                    exception.getMessage()
                            )
                    );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            ApiResponse.error(
                                    "Unable to generate token: "
                                            + exception.getMessage()
                            )
                    );
        }
    }


    // ==========================================
    // VIEW / FILTER CURRENT QUEUE
    // ==========================================

    @GetMapping
    @Operation(
            summary = "View consultation queue",
            description = """
                    Returns the current consultation waiting queue.

                    Examples:

                    /api/tokens
                    /api/tokens?type=NORMAL
                    /api/tokens?type=EMERGENCY
                    """
    )
    public ResponseEntity<ApiResponse<?>>
    getTokens(
            @RequestParam(required = false)
            String type) {


        // EMERGENCY QUEUE
        if (type != null &&
                type.equalsIgnoreCase("EMERGENCY")) {

            List<Token> tokens =
                    tokenService.getEmergencyQueue();


            return ResponseEntity.ok(
                    ApiResponse.success(
                            tokens,
                            Map.of(
                                    "count",
                                    tokens.size(),

                                    "type",
                                    "EMERGENCY"
                            )
                    )
            );
        }


        // NORMAL QUEUE
        if (type != null &&
                type.equalsIgnoreCase("NORMAL")) {

            List<Token> tokens =
                    tokenService.getWaitingQueue();


            return ResponseEntity.ok(
                    ApiResponse.success(
                            tokens,
                            Map.of(
                                    "count",
                                    tokens.size(),

                                    "type",
                                    "NORMAL"
                            )
                    )
            );
        }


        // INVALID TYPE
        if (type != null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            ApiResponse.error(
                                    "Invalid token type. Use NORMAL or EMERGENCY"
                            )
                    );
        }


        // ALL CURRENT WAITING TOKENS
        List<Token> emergencyTokens =
                tokenService.getEmergencyQueue();

        List<Token> normalTokens =
                tokenService.getWaitingQueue();


        List<Token> allTokens =
                new ArrayList<>();

        allTokens.addAll(emergencyTokens);
        allTokens.addAll(normalTokens);


        return ResponseEntity.ok(
                ApiResponse.success(
                        allTokens,
                        Map.of(
                                "count",
                                allTokens.size(),

                                "normalCount",
                                normalTokens.size(),

                                "emergencyCount",
                                emergencyTokens.size()
                        )
                )
        );
    }


    // ==========================================
    // UPDATE TOKEN STATUS
    // ==========================================

    @PutMapping("/{id}")
    @Operation(
            summary = "Update token status",
            description = """
                    Updates the consultation workflow.

                    Supported statuses:

                    IN_CONSULTATION
                    COMPLETED
                    CANCELLED

                    CALLED is handled through the Call Next Patient API.
                    """
    )
    public ResponseEntity<ApiResponse<?>>
    updateTokenStatus(
            @PathVariable Long id,
            @RequestBody TokenStatusRequest request) {


        if (request.getStatus() == null ||
                request.getStatus()
                        .trim()
                        .isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            ApiResponse.error(
                                    "Status is required"
                            )
                    );
        }


        String status =
                request.getStatus()
                        .trim()
                        .toUpperCase();


        boolean updated;


        switch (status) {

            case "IN_CONSULTATION":

                updated =
                        tokenService.startConsultation(id);


                if (!updated) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    ApiResponse.error(
                                            "Consultation could not be started"
                                    )
                            );
                }


                return ResponseEntity.ok(
                        ApiResponse.success(
                                "Consultation started successfully",
                                Map.of(
                                        "tokenId", id,
                                        "status", "IN_CONSULTATION"
                                )
                        )
                );


            case "COMPLETED":

                updated =
                        tokenService.completeToken(id);


                if (!updated) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    ApiResponse.error(
                                            "Consultation could not be completed"
                                    )
                            );
                }


                return ResponseEntity.ok(
                        ApiResponse.success(
                                "Consultation completed successfully",
                                Map.of(
                                        "tokenId", id,
                                        "status", "COMPLETED"
                                )
                        )
                );


            case "CANCELLED":

                updated =
                        tokenService.cancelToken(id);


                if (!updated) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    ApiResponse.error(
                                            "Token could not be cancelled"
                                    )
                            );
                }


                return ResponseEntity.ok(
                        ApiResponse.success(
                                "Token cancelled successfully",
                                Map.of(
                                        "tokenId", id,
                                        "status", "CANCELLED"
                                )
                        )
                );


            default:

                return ResponseEntity
                        .badRequest()
                        .body(
                                ApiResponse.error(
                                        "Invalid status. Use IN_CONSULTATION, COMPLETED or CANCELLED"
                                )
                        );
        }
    }


    // ==========================================
    // CALL NEXT PATIENT
    // ==========================================

    @PutMapping("/doctor/{doctorId}/call-next")
    @Operation(
            summary = "Call next patient",
            description = "Selects the next waiting patient for a doctor according to emergency priority and queue order"
    )
    public ResponseEntity<ApiResponse<?>>
    callNextPatient(
            @PathVariable Long doctorId) {


        Token token =
                tokenService.callNextPatient(
                        doctorId
                );


        if (token == null) {

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "No patients are currently waiting for this doctor",
                            Map.of(
                                    "doctorId",
                                    doctorId
                            )
                    )
            );
        }


        return ResponseEntity.ok(
                ApiResponse.success(
                        token,
                        Map.of(
                                "doctorId",
                                doctorId,

                                "status",
                                "CALLED"
                        )
                )
        );
    }


    // ==========================================
    // PATIENT TOKEN HISTORY
    // ==========================================

    @GetMapping("/history/{patientId}")
    @Operation(
            summary = "View patient history",
            description = "Returns previous consultation token records for the selected patient"
    )
    public ResponseEntity<ApiResponse<?>>
    getPatientHistory(
            @PathVariable Long patientId) {


        List<Token> history =
                tokenService.getPatientHistory(
                        patientId
                );


        return ResponseEntity.ok(
                ApiResponse.success(
                        history,
                        Map.of(
                                "patientId",
                                patientId,

                                "count",
                                history.size()
                        )
                )
        );
    }
}