package com.jcbmanagement.support.controller;

import com.jcbmanagement.support.model.Ticket;
import com.jcbmanagement.support.model.TicketResponse;
import com.jcbmanagement.support.model.TicketStatus;
import com.jcbmanagement.user.model.User;
import com.jcbmanagement.support.service.TicketService;
import com.jcbmanagement.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/support")
public class TicketController {
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userService.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping("/new")
    public String newTicketForm(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("user", user);
        return "user/support/new-ticket";
    }

    @PostMapping("/new")
    public String submitTicket(@ModelAttribute Ticket ticket, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Please log in to create a ticket");
                return "redirect:/login";
            }
            
            ticketService.createTicket(ticket, user);
            redirectAttributes.addFlashAttribute("success", "Ticket created successfully!");
            return "redirect:/support/my-tickets";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating ticket: " + e.getMessage());
            return "redirect:/support/new";
        }
    }

    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Please log in to view tickets");
                return "redirect:/login";
            }
            
            ticketService.getTicketById(id, user).ifPresentOrElse(ticket -> {
                model.addAttribute("ticket", ticket);
                model.addAttribute("responses", ticket.getResponses());
                model.addAttribute("newResponse", new TicketResponse());
                model.addAttribute("user", user);
            }, () -> {
                throw new RuntimeException("Ticket not found or access denied");
            });
            return "user/support/ticket-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error accessing ticket: " + e.getMessage());
            return "redirect:/support/my-tickets";
        }
    }

    @PostMapping("/{id}/respond")
    public String addResponse(@PathVariable Long id, @RequestParam String message, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Please log in to respond to tickets");
                return "redirect:/login";
            }
            
            ticketService.addResponse(id, message, user);
            redirectAttributes.addFlashAttribute("success", "Response added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding response: " + e.getMessage());
        }
        return "redirect:/support/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam TicketStatus status, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Please log in to update ticket status");
                return "redirect:/login";
            }
            
            ticketService.updateStatus(id, status, user);
            redirectAttributes.addFlashAttribute("success", "Status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating status: " + e.getMessage());
        }
        return "redirect:/support/" + id;
    }

    @GetMapping("/my-tickets")
    public String myTickets(Model model) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                model.addAttribute("error", "Please log in to view your tickets");
                return "redirect:/login";
            }
            
            model.addAttribute("tickets", ticketService.getTicketsForUser(user));
            model.addAttribute("user", user);
            return "user/support/my-tickets";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading tickets: " + e.getMessage());
            return "user/support/my-tickets";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Please log in to delete tickets");
                return "redirect:/login";
            }
            
            ticketService.deleteTicket(id, user);
            redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting ticket: " + e.getMessage());
        }
        return "redirect:/support/my-tickets";
    }
}
