package com.jcbmanagement.invoice.service;

import com.jcbmanagement.invoice.model.Invoice;
import com.jcbmanagement.invoice.model.PaymentSlip;
import com.jcbmanagement.invoice.repository.InvoiceRepository;
import com.jcbmanagement.invoice.repository.PaymentSlipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private PaymentSlipRepository paymentSlipRepository;

    public Invoice generateInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }
    
    public PaymentSlip savePaymentSlip(PaymentSlip paymentSlip) {
        return paymentSlipRepository.save(paymentSlip);
    }
    
    public List<PaymentSlip> getAllPaymentSlips() {
        return paymentSlipRepository.findAll();
    }
    
    public Optional<PaymentSlip> getPaymentSlipById(Long id) {
        return paymentSlipRepository.findById(id);
    }
    
    public List<PaymentSlip> getPaymentSlipsByBooking(Long bookingId) {
        return paymentSlipRepository.findByBookingId(bookingId);
    }
    
    public PaymentSlip verifyPaymentSlip(Long slipId, String verifiedBy, String status, String remarks) {
        Optional<PaymentSlip> slipOpt = paymentSlipRepository.findById(slipId);
        if (slipOpt.isPresent()) {
            PaymentSlip slip = slipOpt.get();
            slip.setStatus(status);
            slip.setVerifiedBy(verifiedBy);
            slip.setRemarks(remarks);
            slip.setVerifiedAt(LocalDateTime.now());
            return paymentSlipRepository.save(slip);
        }
        throw new IllegalArgumentException("Payment slip not found");
    }
}
