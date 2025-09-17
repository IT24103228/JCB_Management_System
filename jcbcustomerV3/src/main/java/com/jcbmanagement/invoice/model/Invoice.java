package com.jcbmanagement.invoice.model;

import com.jcbmanagement.booking.model.Booking;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceID;

    @OneToOne
    @JoinColumn(name = "BookingID")
    private Booking booking;

    private double amount;
    private String status = "GENERATED";
}
