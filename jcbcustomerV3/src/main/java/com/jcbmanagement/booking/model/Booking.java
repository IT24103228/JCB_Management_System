package com.jcbmanagement.booking.model;

import com.jcbmanagement.inventory.model.Machine;
import com.jcbmanagement.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingID;

    @ManyToOne
    @JoinColumn(name = "CustomerID")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "MachineID")
    private Machine machine;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalCost;
    private String notes;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    private boolean paymentVerified = false;
    private String paymentStatus = "PENDING";
    private LocalDateTime paymentVerifiedAt;

    public enum BookingStatus {
        PENDING("Pending Approval"),
        CONFIRMED("Confirmed"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String displayName;

        BookingStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
