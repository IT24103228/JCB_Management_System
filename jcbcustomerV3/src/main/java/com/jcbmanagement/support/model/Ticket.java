package com.jcbmanagement.support.model;

import com.jcbmanagement.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketID;

    @ManyToOne
    @JoinColumn(name = "CustomerID")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "AssignedStaffID")
    private User assignedStaff;

    private String subject;
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketCategory category = TicketCategory.BOOKING;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.OPEN;

    private boolean flagged = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<TicketResponse> responses = new ArrayList<>();

    public enum TicketCategory {
        PAYMENT("Payment Issues"),
        BOOKING("Booking Issues");

        private final String displayName;

        TicketCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
