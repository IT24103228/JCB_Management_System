package com.jcbmanagement.dashboard.controller;

import com.jcbmanagement.user.model.User;
import com.jcbmanagement.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        addUserToModel(model);
        return "dashboard/admin";
    }

    @GetMapping("/finance/dashboard")
    public String financeDashboard(Model model) {
        addUserToModel(model);
        return "dashboard/finance";
    }

    @GetMapping("/booking/dashboard")
    public String bookingDashboard(Model model) {
        addUserToModel(model);
        return "dashboard/booking";
    }

    @GetMapping("/inventory/dashboard")
    public String inventoryDashboard(Model model) {
        addUserToModel(model);
        return "dashboard/inventory";
    }

    @GetMapping("/maintenance/dashboard")
    public String maintenanceDashboard(Model model) {
        addUserToModel(model);
        return "dashboard/maintenance";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(Model model) {
        addUserToModel(model);
        return "dashboard/customer";
    }

    private void addUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username).orElse(null);
        model.addAttribute("user", user);
    }
}
