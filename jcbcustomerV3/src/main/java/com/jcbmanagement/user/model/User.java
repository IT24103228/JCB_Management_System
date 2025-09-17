package com.jcbmanagement.user.model;

import com.jcbmanagement.support.model.Ticket;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userID;

    @Column(name = "Username", unique = true, nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Column(name = "Password", nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Column(name = "Role", nullable = false)
    @NotBlank(message = "Role is required")
    private String role;  // 'CUSTOMER', 'ADMIN', 'FINANCE_OFFICER', 'BOOKING_MANAGER', 'INVENTORY_MANAGER', 'MAINTENANCE_SUPERVISOR'

    @Column(name = "Email")
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Column(name = "CreatedAt", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Ticket> ticketsAsCustomer;

    public enum Role {
        CUSTOMER("customer"),
        ADMIN("admin"),
        FINANCE_OFFICER("finance officer"),
        BOOKING_MANAGER("booking manager"),
        INVENTORY_MANAGER("inventory manager"),
        MAINTENANCE_SUPERVISOR("maintenance supervisor");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Role fromDisplayName(String displayName) {
            for (Role role : Role.values()) {
                if (role.displayName.equals(displayName)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Invalid role: " + displayName);
        }
    }
}
