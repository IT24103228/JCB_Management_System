package com.jcbmanagement.booking.controller;

import com.jcbmanagement.booking.model.Booking;
import com.jcbmanagement.booking.service.BookingService;
import com.jcbmanagement.inventory.service.MachineService;
import com.jcbmanagement.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/booking")
public class BookingController {
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private MachineService machineService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/new")
    public String newBookingForm(Model model) {
        model.addAttribute("booking", new Booking());
        model.addAttribute("machines", machineService.getAvailableMachines());
        return "booking/new";
    }

    @PostMapping("/new")
    public String submitBooking(@Valid @ModelAttribute Booking booking, 
                               BindingResult result, 
                               Principal principal,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("machines", machineService.getAvailableMachines());
            return "booking/new";
        }
        
        try {
            // Set the customer from the logged-in user
            if (principal != null) {
                userService.findByUsername(principal.getName())
                    .ifPresent(booking::setCustomer);
            }
            
            bookingService.createBooking(booking);
            redirectAttributes.addFlashAttribute("successMessage", "Booking created successfully!");
            return "redirect:/booking/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("machines", machineService.getAvailableMachines());
            return "booking/new";
        }
    }
    
    @GetMapping("/list")
    public String listBookings(Model model) {
        List<Booking> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        return "booking/list";
    }
    
    @GetMapping("/{id}")
    public String viewBooking(@PathVariable Long id, Model model) {
        return bookingService.getBookingById(id)
            .map(booking -> {
                model.addAttribute("booking", booking);
                return "booking/view";
            })
            .orElse("redirect:/booking/list");
    }
    
    @PostMapping("/{id}/status")
    public String updateBookingStatus(@PathVariable Long id, 
                                    @RequestParam Booking.BookingStatus status,
                                    RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateBookingStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Booking status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/booking/" + id;
    }
    
    @GetMapping("/pending")
    public String pendingBookings(Model model) {
        List<Booking> pendingBookings = bookingService.getPendingBookings();
        model.addAttribute("bookings", pendingBookings);
        model.addAttribute("title", "Pending Bookings");
        return "booking/pending";
    }
    
    @PostMapping("/{id}/approve")
    public String approveBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateBookingStatus(id, Booking.BookingStatus.CONFIRMED);
            redirectAttributes.addFlashAttribute("successMessage", "Booking approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/booking/pending";
    }
    
    @PostMapping("/{id}/reject")
    public String rejectBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateBookingStatus(id, Booking.BookingStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("successMessage", "Booking rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/booking/pending";
    }
}
