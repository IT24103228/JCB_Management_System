package com.jcbmanagement.support.model;

import com.jcbmanagement.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "TicketResponses")
public class TicketResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseID;

    @ManyToOne
    @JoinColumn(name = "TicketID")
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();
}
