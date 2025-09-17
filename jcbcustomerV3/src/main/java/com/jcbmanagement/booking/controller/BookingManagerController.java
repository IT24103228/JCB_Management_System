package com.jcbmanagement.booking.controller;

import com.jcbmanagement.booking.service.BookingService;
import com.jcbmanagement.support.service.TicketService;
import com.jcbmanagement.support.model.Ticket;
import com.jcbmanagement.support.model.TicketStatus;
import com.jcbmanagement.inventory.service.MachineService;
import com.jcbmanagement.user.model.User;
import com.jcbmanagement.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/booking")
public class BookingManagerController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private MachineService machineService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/tickets")
    public String manageTickets(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User bookingManager = userService.findByUsername(username).orElse(null);
        
        if (bookingManager != null) {
            List<Ticket> assignedTickets = ticketService.getTicketsForUser(bookingManager);
            model.addAttribute("tickets", assignedTickets);
        }
        
        return "booking/tickets";
    }
    
    @GetMapping("/tickets/{id}")
    public String viewTicket(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User bookingManager = userService.findByUsername(username).orElse(null);
        
        if (bookingManager != null) {
            ticketService.getTicketById(id, bookingManager).ifPresent(ticket -> {
                model.addAttribute("ticket", ticket);
                model.addAttribute("responses", ticket.getResponses());
            });
        }
        
        return "booking/ticket-details";
    }
    
    @PostMapping("/tickets/{id}/status")
    public String updateTicketStatus(@PathVariable Long id, 
                                   @RequestParam TicketStatus status,
                                   RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User bookingManager = userService.findByUsername(username).orElse(null);
            
            if (bookingManager != null) {
                ticketService.updateStatus(id, status, bookingManager);
                redirectAttributes.addFlashAttribute("successMessage", "Ticket status updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/booking/tickets/" + id;
    }
    
    @PostMapping("/tickets/{id}/flag")
    public String flagTicketForAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User bookingManager = userService.findByUsername(username).orElse(null);
            
            if (bookingManager != null) {
                ticketService.flagTicketForAdmin(id, bookingManager);
                redirectAttributes.addFlashAttribute("successMessage", "Ticket flagged for admin review!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/booking/tickets/" + id;
    }
    
    @PostMapping("/tickets/{id}/respond")
    public String addTicketResponse(@PathVariable Long id, 
                                  @RequestParam String message,
                                  RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User bookingManager = userService.findByUsername(username).orElse(null);
            
            if (bookingManager != null) {
                ticketService.addResponse(id, message, bookingManager);
                redirectAttributes.addFlashAttribute("successMessage", "Response added successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/booking/tickets/" + id;
    }
}
