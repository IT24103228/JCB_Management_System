package com.jcbmanagement.dashboard.controller;

import com.jcbmanagement.user.model.User;
import com.jcbmanagement.user.service.UserService;
import com.jcbmanagement.booking.model.Booking;
import com.jcbmanagement.booking.service.BookingService;
import com.jcbmanagement.inventory.service.MachineService;
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

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MachineService machineService;

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
        try {
            addUserToModel(model);
            
            model.addAttribute("totalBookings", bookingService.countTotalBookings());
            model.addAttribute("pendingBookings", bookingService.countBookingsByStatus(Booking.BookingStatus.PENDING));
            model.addAttribute("availableMachines", machineService.getAvailableMachines().size());
            
            return "dashboard/booking";
        } catch (Exception e) {
            addUserToModel(model);
            model.addAttribute("totalBookings", 0);
            model.addAttribute("pendingBookings", 0);
            model.addAttribute("availableMachines", 0);
            return "dashboard/booking";
        }
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
        try {
            addUserToModel(model);
            return "dashboard/customer";
        } catch (Exception e) {
            // Fallback: add a default user object to prevent template errors
            model.addAttribute("user", new User());
            return "dashboard/customer";
        }
    }

    private void addUserToModel(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) {
                model.addAttribute("user", new User());
                return;
            }
            
            String username = auth.getName();
            
            User user = userService.findByUsername(username).orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername(username);
            }
            
            model.addAttribute("user", user);
        } catch (Exception e) {
            // Fallback: add a default user object
            User defaultUser = new User();
            defaultUser.setUsername("Unknown User");
            model.addAttribute("user", defaultUser);
        }
    }
}
