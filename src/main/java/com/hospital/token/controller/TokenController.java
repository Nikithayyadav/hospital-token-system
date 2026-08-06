package com.hospital.token.controller;

import com.hospital.token.model.Patient;
import com.hospital.token.model.Token;
import com.hospital.token.service.DepartmentService;
import com.hospital.token.service.DoctorService;
import com.hospital.token.service.PatientService;
import com.hospital.token.service.TokenService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/tokens")
public class TokenController {


    // ==========================================
    // SERVICES
    // ==========================================

    private final TokenService tokenService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final DepartmentService departmentService;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public TokenController(
            TokenService tokenService,
            PatientService patientService,
            DoctorService doctorService,
            DepartmentService departmentService) {

        this.tokenService = tokenService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.departmentService = departmentService;
    }


    // ==========================================
    // NORMAL TOKEN PAGE
    // ==========================================

    @GetMapping("/generate")
    public String generateTokenPage(
            @RequestParam(required = false) Long departmentId,
            HttpSession session,
            Model model) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }


        List<Patient> patients =
                patientService.getAllPatients();


        model.addAttribute(
                "patients",
                patients
        );


        model.addAttribute(
                "departments",
                departmentService.getActiveDepartments()
        );


        if (departmentId != null) {

            model.addAttribute(
                    "doctors",
                    doctorService.getDoctorsByDepartment(
                            departmentId
                    )
            );

        } else {

            model.addAttribute(
                    "doctors",
                    doctorService.getAllDoctors()
            );
        }


        model.addAttribute(
                "selectedDepartment",
                departmentId
        );


        return "generate-token";
    }


    // ==========================================
    // GENERATE NORMAL TOKEN
    // ==========================================

    @PostMapping("/generate")
    public String generateToken(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam String reasonForVisit,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }


        try {

            Token token =
                    tokenService.generateNormalToken(
                            patientId,
                            doctorId,
                            reasonForVisit
                    );


            return "redirect:/tokens/success/"
                    + token.getId();


        } catch (Exception exception) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );


            return "redirect:/tokens/generate";
        }
    }

        // Unified generation endpoint for NORMAL or EMERGENCY from existing patients
        @PostMapping("/generate-any")
        public String generateAnyToken(
                        @RequestParam Long patientId,
                        @RequestParam Long doctorId,
                        @RequestParam String consultationType,
                        @RequestParam(required = false) String reasonForVisit,
                        @RequestParam(required = false) String priority,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                if (session.getAttribute("loggedInStaff") == null) {
                        return "redirect:/";
                }

                try {
                        Token token;

                        if ("EMERGENCY".equalsIgnoreCase(consultationType)) {
                                token = tokenService.generateEmergencyToken(
                                                patientId,
                                                doctorId,
                                                reasonForVisit == null ? "" : reasonForVisit,
                                                priority == null ? "LOW" : priority
                                );
                        } else {
                                token = tokenService.generateNormalToken(
                                                patientId,
                                                doctorId,
                                                reasonForVisit == null ? "" : reasonForVisit
                                );
                        }

                        return "redirect:/tokens/success/" + token.getId();

                } catch (Exception ex) {
                        redirectAttributes.addFlashAttribute("error", ex.getMessage());
                        return "redirect:/tokens/generate";
                }
        }

        @GetMapping("/queue")
        public String queueManagement(HttpSession session, Model model) {

                if (session.getAttribute("loggedInStaff") == null) {
                        return "redirect:/";
                }

                model.addAttribute("waitingQueue", tokenService.getWaitingQueue());
                model.addAttribute("emergencyQueue", tokenService.getEmergencyQueue());
                model.addAttribute("doctors", doctorService.getAllDoctors());

                return "queue-management";
        }


    // ==========================================
    // TOKEN SUCCESS PAGE
    // ==========================================

    @GetMapping("/success/{id}")
    public String tokenSuccess(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }


        Token token =
                tokenService.getToken(id);


        if (token == null) {
            return "redirect:/dashboard";
        }


        model.addAttribute(
                "token",
                token
        );


        return "token-success";
    }


    // ==========================================
    // EMERGENCY TOKEN PAGE
    // ==========================================

    @GetMapping("/emergency")
    public String emergencyTokenPage(
            @RequestParam(required = false) Long departmentId,
            HttpSession session,
            Model model) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }


        model.addAttribute(
                "patients",
                patientService.getAllPatients()
        );


        model.addAttribute(
                "departments",
                departmentService.getActiveDepartments()
        );


        if (departmentId != null) {

            model.addAttribute(
                    "doctors",
                    doctorService.getDoctorsByDepartment(
                            departmentId
                    )
            );

        } else {

            model.addAttribute(
                    "doctors",
                    doctorService.getAllDoctors()
            );
        }


        model.addAttribute(
                "selectedDepartment",
                departmentId
        );


        return "emergency-token";
    }


    // ==========================================
    // GENERATE EMERGENCY TOKEN
    // ==========================================

    @PostMapping("/emergency")
    public String generateEmergencyToken(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam String reasonForVisit,
            @RequestParam String priority,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }


        try {

            Token token =
                    tokenService.generateEmergencyToken(
                            patientId,
                            doctorId,
                            reasonForVisit,
                            priority
                    );


            return "redirect:/tokens/success/"
                    + token.getId();


        } catch (Exception exception) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );


            return "redirect:/tokens/emergency";
        }
    }


    // ==========================================
    // CALL NEXT PATIENT
    // ==========================================

    @PostMapping("/doctor/{doctorId}/call-next")
    public String callNextPatient(
            @PathVariable Long doctorId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }


        Token next =
                tokenService.callNextPatient(
                        doctorId
                );


        if (next == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No patients are waiting for this doctor."
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Patient called successfully. Token: "
                            + next.getTokenNumber()
            );
        }


        return "redirect:/dashboard";
    }


    // ==========================================
    // START CONSULTATION
    // ==========================================

    @PostMapping("/{id}/start")
    public String startConsultation(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }


        boolean started =
                tokenService.startConsultation(id);


        if (!started) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Could not start consultation."
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Consultation started successfully."
            );
        }


        return "redirect:/dashboard";
    }


    // ==========================================
    // COMPLETE CONSULTATION
    // ==========================================

    @PostMapping("/{id}/complete")
    public String completeToken(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }


        boolean completed =
                tokenService.completeToken(id);


        if (!completed) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Could not complete consultation."
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Consultation completed successfully."
            );
        }


        return "redirect:/dashboard";
    }


    // ==========================================
    // CANCEL TOKEN
    // ==========================================

    @PostMapping("/{id}/cancel")
    public String cancelToken(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }


        boolean cancelled =
                tokenService.cancelToken(id);


        if (!cancelled) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Could not cancel token."
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Token cancelled successfully."
            );
        }


        return "redirect:/dashboard";
    }

}