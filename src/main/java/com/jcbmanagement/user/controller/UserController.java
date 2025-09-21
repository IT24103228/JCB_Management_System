package com.jcbmanagement.user.controller;

import com.jcbmanagement.user.model.User;
import com.jcbmanagement.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String registerForm() {
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password,
                           @RequestParam String email, Model model) {
        try {
            String normalizedRole = "CUSTOMER";
            userService.registerUser(username, password, normalizedRole, email);
            model.addAttribute("message", "Registration successful! Please login.");
            return "user/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "user/register";
        }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "user/login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElse(null);

        if (user != null) {
            String role = user.getRole();
            switch (role) {
                case "ADMIN":
                    return "redirect:/admin/dashboard";
                case "FINANCE_OFFICER":
                    return "redirect:/finance/dashboard";
                case "BOOKING_MANAGER":
                    return "redirect:/booking/dashboard";
                case "INVENTORY_MANAGER":
                    return "redirect:/inventory/dashboard";
                case "MAINTENANCE_SUPERVISOR":
                    return "redirect:/maintenance/dashboard";
                case "CUSTOMER":
                default:
                    return "redirect:/customer/dashboard";
            }
        }
        return "redirect:/login";
    }
}
