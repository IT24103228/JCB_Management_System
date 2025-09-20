package com.jcbmanagement.invoice.repository;

import com.jcbmanagement.invoice.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
