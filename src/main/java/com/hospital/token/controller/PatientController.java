package com.hospital.token.controller;

import com.hospital.token.model.Patient;
import com.hospital.token.model.Token;
import com.hospital.token.service.PatientService;
import com.hospital.token.service.TokenService;
import com.hospital.token.service.DepartmentService;
import com.hospital.token.service.DoctorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;
    private final TokenService tokenService;
    private final DepartmentService departmentService;
    private final DoctorService doctorService;

    public PatientController(
            PatientService patientService,
            TokenService tokenService,
            DepartmentService departmentService,
            DoctorService doctorService) {

        this.patientService = patientService;
        this.tokenService = tokenService;
        this.departmentService = departmentService;
        this.doctorService = doctorService;
    }


    private boolean notLoggedIn(HttpSession session) {

        return session.getAttribute("loggedInStaff") == null;
    }


    @GetMapping
    public String patients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            HttpSession session,
            Model model) {

        if (notLoggedIn(session)) {
            return "redirect:/";
        }

        List<Patient> patients;

        if (search != null && !search.trim().isEmpty()) {

            patients =
                    patientService.searchPatients(search);

        } else {

            patients =
                    patientService.getAllPatients();
        }

        if ("name".equals(sort)) {

            patients =
                    patientService
                            .getPatientsAlphabetically()
                            .stream()
                            .toList();

        } else if ("id".equals(sort)) {

            patients =
                    patientService
                            .getPatientsById()
                            .values()
                            .stream()
                            .toList();
        }

        model.addAttribute("patients", patients);
        model.addAttribute("search", search);

        return "patients";
    }


    @GetMapping("/register")
    public String registrationPage(
            HttpSession session,
            Model model) {

        if (notLoggedIn(session)) {
            return "redirect:/";
        }

        model.addAttribute(
            "patient",
            new Patient()
        );

        // allow pre-filtering doctors when arriving from tokens page
        String deptParam = null;
        model.addAttribute("departments", departmentService.getActiveDepartments());
        model.addAttribute("doctors", doctorService.getAllDoctors());

        model.addAttribute(
            "departments",
            departmentService.getActiveDepartments()
        );

        model.addAttribute(
            "doctors",
            doctorService.getAllDoctors()
        );

        return "register-patient";
    }


    @PostMapping("/register")
        public String register(
            @ModelAttribute Patient patient,
            @RequestParam(required = false) String consultationType,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String reasonForVisit,
            @RequestParam(required = false) String priority,
            HttpSession session) {

        if (notLoggedIn(session)) {
            return "redirect:/";
        }

        Patient saved = patientService.registerPatient(patient);

        // If receptionist requested immediate token generation
        if (consultationType != null && !consultationType.isBlank()
                && doctorId != null) {

            try {
                if ("EMERGENCY".equalsIgnoreCase(consultationType)) {

                    Token token = tokenService.generateEmergencyToken(
                            saved.getId(),
                            doctorId,
                            reasonForVisit == null ? "" : reasonForVisit,
                            priority == null ? "LOW" : priority
                    );

                    return "redirect:/tokens/success/" + token.getId();

                } else {

                    Token token = tokenService.generateNormalToken(
                            saved.getId(),
                            doctorId,
                            reasonForVisit == null ? "" : reasonForVisit
                    );

                    return "redirect:/tokens/success/" + token.getId();
                }

            } catch (Exception ex) {

                // ignore token generation errors; fall back to patient list
            }
        }

        return "redirect:/patients?registered=true";
    }


    @GetMapping("/edit/{id}")
    public String editPage(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (notLoggedIn(session)) {
            return "redirect:/";
        }

        Patient patient =
                patientService.getPatient(id);

        if (patient == null) {
            return "redirect:/patients";
        }

        model.addAttribute("patient", patient);

        return "edit-patient";
    }


    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute Patient patient,
            HttpSession session) {

        if (notLoggedIn(session)) {
            return "redirect:/";
        }

        patientService.updatePatient(id, patient);

        return "redirect:/patients?updated=true";
    }


    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            HttpSession session) {

        if (notLoggedIn(session)) {
            return "redirect:/";
        }

        patientService.deletePatient(id);

        return "redirect:/patients?deleted=true";
    }
}