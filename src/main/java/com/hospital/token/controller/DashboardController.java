package com.hospital.token.controller;

import com.hospital.token.model.Staff;
import com.hospital.token.service.DoctorService;
import com.hospital.token.service.PatientService;
import com.hospital.token.service.TokenService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@Controller
public class DashboardController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final TokenService tokenService;

    public DashboardController(
            PatientService patientService,
            DoctorService doctorService,
            TokenService tokenService) {

        this.patientService = patientService;
        this.doctorService = doctorService;
        this.tokenService = tokenService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        Staff staff =
                (Staff) session.getAttribute("loggedInStaff");

        if (staff == null) {
            return "redirect:/";
        }

        model.addAttribute("staff", staff);

        model.addAttribute(
                "totalPatients",
                patientService.getTotalPatientCount()
        );

        model.addAttribute(
                "availableDoctors",
                doctorService.getAvailableDoctorCount()
        );
        model.addAttribute(
                "waitingCount",
                tokenService.getWaitingCount()
        );

        model.addAttribute(
                "emergencyCount",
                tokenService.getEmergencyCount()
        );

        model.addAttribute(
                "doctors",
                doctorService.getAllDoctors()
        );

        // per-doctor current and next tokens
        Map<Long, com.hospital.token.model.Token> currentMap = new java.util.HashMap<>();
        Map<Long, com.hospital.token.model.Token> nextMap = new java.util.HashMap<>();

        for (com.hospital.token.model.Doctor d : doctorService.getAllDoctors()) {
            currentMap.put(d.getId(), tokenService.getCurrentTokenForDoctor(d.getId()));
            nextMap.put(d.getId(), tokenService.getNextWaitingForDoctor(d.getId()));
        }

        model.addAttribute("currentTokenMap", currentMap);
        model.addAttribute("nextTokenMap", nextMap);

        return "dashboard";
    }
}