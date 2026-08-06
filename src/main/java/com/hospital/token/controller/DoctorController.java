package com.hospital.token.controller;

import com.hospital.token.model.DoctorStatus;
import com.hospital.token.service.DoctorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(
            DoctorService doctorService) {

        this.doctorService = doctorService;
    }

    @GetMapping
    public String doctors(
            HttpSession session,
            Model model) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }

        model.addAttribute(
                "doctors",
                doctorService.getAllDoctors()
        );

        model.addAttribute(
                "statuses",
                DoctorStatus.values()
        );

        return "doctors";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam DoctorStatus status,
            HttpSession session) {

        if (session.getAttribute("loggedInStaff") == null) {
            return "redirect:/";
        }

        doctorService.updateDoctorStatus(id, status);

        return "redirect:/doctors?updated=true";
    }
}