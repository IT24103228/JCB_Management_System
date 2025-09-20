package com.jcbmanagement.invoice.controller;

import com.jcbmanagement.invoice.model.Invoice;
import com.jcbmanagement.invoice.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/invoice")
public class InvoiceController {
    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/new")
    public String newInvoiceForm() {
        return "invoice/new";
    }

    @PostMapping("/new")
    public String submitInvoice(@ModelAttribute Invoice invoice) {
        invoiceService.generateInvoice(invoice);
        return "redirect:/invoice/list";
    }
}
