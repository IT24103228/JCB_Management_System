package com.jcbmanagement.support.service;

import com.jcbmanagement.support.model.Ticket;
import com.jcbmanagement.support.model.TicketResponse;
import com.jcbmanagement.support.model.TicketStatus;
import com.jcbmanagement.user.model.User;
import com.jcbmanagement.user.repository.UserRepository;
import com.jcbmanagement.support.repository.TicketRepository;
import com.jcbmanagement.support.repository.TicketResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketResponseRepository responseRepository;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('CUSTOMER')")
    public Ticket createTicket(Ticket ticket, User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be logged in to create a ticket");
        }

        if (currentUser.getUserID() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (ticket.getSubject() == null || ticket.getDescription() == null ||
                ticket.getSubject().trim().isEmpty() || ticket.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject and description are required");
        }

        ticket.setCustomer(currentUser);
        ticket.setAssignedStaff(assignStaff());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    private User assignStaff() {
        try {
            List<User> staff = userRepository.findByRole("BOOKING_MANAGER");
            if (staff.isEmpty()) {
                return null;
            }
            return staff.get(0);  // Simple assignment
        } catch (Exception e) {
            return null;
        }
    }

    public TicketResponse addResponse(Long ticketId, String message, User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be logged in to add a response");
        }

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message is required");
        }
        Optional<Ticket> optTicket = ticketRepository.findById(ticketId);
        if (optTicket.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }
        Ticket ticket = optTicket.get();

        boolean isAuthorized = ticket.getCustomer().equals(currentUser) ||
                (ticket.getAssignedStaff() != null && ticket.getAssignedStaff().equals(currentUser)) ||
                "ADMIN".equals(currentUser.getRole());

        if (!isAuthorized) {
            throw new SecurityException("Not authorized to respond to this ticket");
        }

        TicketResponse response = new TicketResponse();
        response.setTicket(ticket);
        response.setUser(currentUser);
        response.setMessage(message.trim());
        response.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        return responseRepository.save(response);
    }

    public Ticket updateStatus(Long ticketId, TicketStatus status, User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be logged in to update ticket status");
        }

        Optional<Ticket> optTicket = ticketRepository.findById(ticketId);
        if (optTicket.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = optTicket.get();

        // Allow booking managers and admins to update status
        if (!currentUser.getRole().equals("BOOKING_MANAGER") && !currentUser.getRole().equals("ADMIN")) {
            throw new IllegalArgumentException("Not authorized to update ticket status");
        }

        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public Ticket flagTicketForAdmin(Long ticketId, User currentUser) {
        Optional<Ticket> optTicket = ticketRepository.findById(ticketId);
        if (optTicket.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = optTicket.get();

        // Only booking managers can flag tickets for admin
        if (!currentUser.getRole().equals("BOOKING_MANAGER")) {
            throw new IllegalArgumentException("Not authorized to flag tickets");
        }

        ticket.setFlagged(true);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public Ticket unflagTicket(Long ticketId, User currentUser) {
        Optional<Ticket> optTicket = ticketRepository.findById(ticketId);
        if (optTicket.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = optTicket.get();

        // Only admins can unflag tickets
        if (!currentUser.getRole().equals("ADMIN")) {
            throw new IllegalArgumentException("Not authorized to unflag tickets");
        }

        ticket.setFlagged(false);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public Ticket reassignTicket(Long ticketId, Long newStaffId, User currentUser) {
        Optional<Ticket> optTicket = ticketRepository.findById(ticketId);
        if (optTicket.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Optional<User> newStaffOpt = userRepository.findById(newStaffId);
        if (newStaffOpt.isEmpty()) {
            throw new IllegalArgumentException("Staff member not found");
        }

        Ticket ticket = optTicket.get();
        User newStaff = newStaffOpt.get();

        // Only admins can reassign tickets
        if (!currentUser.getRole().equals("ADMIN")) {
            throw new IllegalArgumentException("Not authorized to reassign tickets");
        }

        // Ensure new staff is a booking manager
        if (!newStaff.getRole().equals("BOOKING_MANAGER")) {
            throw new IllegalArgumentException("Can only assign tickets to booking managers");
        }

        ticket.setAssignedStaff(newStaff);
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getFlaggedTickets() {
        return ticketRepository.findByFlaggedTrue();
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsForUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if ("CUSTOMER".equals(user.getRole())) {
            return ticketRepository.findByCustomer(user);
        } else if ("BOOKING_MANAGER".equals(user.getRole())) {
            // Get tickets assigned to this booking manager, or unassigned tickets
            List<Ticket> assignedTickets = ticketRepository.findByAssignedStaff(user);
            List<Ticket> unassignedTickets = ticketRepository.findByAssignedStaffIsNull();
            assignedTickets.addAll(unassignedTickets);
            return assignedTickets;
        } else {
            // For admin or other roles, return all tickets
            return ticketRepository.findAll();
        }
    }

    public Optional<Ticket> getTicketById(Long id, User user) {
        if (user == null) {
            return Optional.empty();
        }

        Optional<Ticket> optTicket = ticketRepository.findById(id);
        if (optTicket.isPresent()) {
            Ticket ticket = optTicket.get();
            boolean hasAccess = ticket.getCustomer().equals(user) ||
                    (ticket.getAssignedStaff() != null && ticket.getAssignedStaff().equals(user)) ||
                    "ADMIN".equals(user.getRole());

            if (hasAccess) {
                return optTicket;
            }
        }
        return Optional.empty();
    }

    public void deleteTicket(Long ticketId, User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be logged in to delete a ticket");
        }

        Optional<Ticket> optTicket = ticketRepository.findById(ticketId);
        if (optTicket.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found");
        }

        Ticket ticket = optTicket.get();

        // Only allow customers to delete their own tickets
        if (!ticket.getCustomer().equals(currentUser)) {
            throw new SecurityException("You can only delete your own tickets");
        }

        // Delete the ticket (this will cascade delete responses due to CascadeType.ALL)
        ticketRepository.deleteById(ticketId);
    }
}
