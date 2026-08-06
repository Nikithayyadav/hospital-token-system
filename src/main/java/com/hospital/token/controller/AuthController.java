package com.hospital.token.controller;

import com.hospital.token.model.Staff;
import com.hospital.token.service.StaffService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final StaffService staffService;

    public AuthController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping("/")
    public String showLoginPage(HttpSession session) {

        if (session.getAttribute("loggedInStaff") != null) {
            return "redirect:/dashboard";
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Staff staff = staffService.authenticate(username, password);

        if (staff != null) {

            session.setAttribute("loggedInStaff", staff);

            return "redirect:/dashboard";
        }

        model.addAttribute(
                "error",
                "Invalid username or password."
        );

        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}