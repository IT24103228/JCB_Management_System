package com.jcbmanagement.invoice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "PaymentSlips")
public class PaymentSlip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slipID;

    @OneToOne
    @JoinColumn(name = "InvoiceID")
    private Invoice invoice;

    private Long bookingId;

    private String filePath;
    private String status = "UPLOADED";

    private LocalDateTime uploadedAt = LocalDateTime.now();
    private LocalDateTime verifiedAt;

    private String verifiedBy;
    private String remarks;

    public enum PaymentStatus {
        UPLOADED("Uploaded"),
        VERIFIED("Verified"),
        REJECTED("Rejected"),
        PENDING("Pending Review");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
