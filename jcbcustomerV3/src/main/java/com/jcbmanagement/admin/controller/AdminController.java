package com.jcbmanagement.admin.controller;

import com.jcbmanagement.support.service.TicketService;
import com.jcbmanagement.support.model.Ticket;
import com.jcbmanagement.support.model.TicketStatus;
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
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/flagged-tickets")
    public String manageFlaggedTickets(Model model) {
        List<Ticket> flaggedTickets = ticketService.getFlaggedTickets();
        model.addAttribute("tickets", flaggedTickets);
        return "admin/flagged-tickets";
    }
    
    @GetMapping("/flagged-tickets/{id}")
    public String viewFlaggedTicket(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User admin = userService.findByUsername(username).orElse(null);
        
        if (admin != null) {
            ticketService.getTicketById(id, admin).ifPresent(ticket -> {
                model.addAttribute("ticket", ticket);
                model.addAttribute("responses", ticket.getResponses());
            });
        }
        
        return "admin/flagged-ticket-details";
    }
    
    @PostMapping("/flagged-tickets/{id}/unflag")
    public String unflagTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User admin = userService.findByUsername(username).orElse(null);
            
            if (admin != null) {
                ticketService.unflagTicket(id, admin);
                redirectAttributes.addFlashAttribute("successMessage", "Ticket unflagged successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/flagged-tickets";
    }
    
    @PostMapping("/flagged-tickets/{id}/status")
    public String updateFlaggedTicketStatus(@PathVariable Long id, 
                                          @RequestParam TicketStatus status,
                                          RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User admin = userService.findByUsername(username).orElse(null);
            
            if (admin != null) {
                ticketService.updateStatus(id, status, admin);
                redirectAttributes.addFlashAttribute("successMessage", "Ticket status updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/flagged-tickets/" + id;
    }
    
    @PostMapping("/flagged-tickets/{id}/respond")
    public String addResponseToFlaggedTicket(@PathVariable Long id, 
                                           @RequestParam String message,
                                           RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User admin = userService.findByUsername(username).orElse(null);
            
            if (admin != null) {
                ticketService.addResponse(id, message, admin);
                redirectAttributes.addFlashAttribute("successMessage", "Response added successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/flagged-tickets/" + id;
    }
    
    @PostMapping("/flagged-tickets/{id}/reassign")
    public String reassignTicket(@PathVariable Long id, 
                               @RequestParam Long newStaffId,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User admin = userService.findByUsername(username).orElse(null);
            
            if (admin != null) {
                ticketService.reassignTicket(id, newStaffId, admin);
                redirectAttributes.addFlashAttribute("successMessage", "Ticket reassigned successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/flagged-tickets/" + id;
    }
}
