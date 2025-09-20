package com.jcbmanagement.invoice.repository;

import com.jcbmanagement.invoice.model.PaymentSlip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentSlipRepository extends JpaRepository<PaymentSlip, Long> {
    List<PaymentSlip> findByBookingId(Long bookingId);
    
    List<PaymentSlip> findByStatus(String status);
}
