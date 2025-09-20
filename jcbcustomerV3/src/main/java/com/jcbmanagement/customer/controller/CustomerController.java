package com.jcbmanagement.customer.controller;

import com.jcbmanagement.booking.model.Booking;
import com.jcbmanagement.booking.service.BookingService;
import com.jcbmanagement.inventory.model.Machine;
import com.jcbmanagement.inventory.service.MachineService;
import com.jcbmanagement.user.model.User;
import com.jcbmanagement.user.service.UserService;
import com.jcbmanagement.support.service.TicketService;
import com.jcbmanagement.invoice.model.PaymentSlip;
import com.jcbmanagement.invoice.service.InvoiceService;
import com.jcbmanagement.support.model.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private MachineService machineService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private InvoiceService invoiceService;
    
    private static final String UPLOAD_DIR = "uploads/payment-slips/";
    
    @GetMapping("/bookings")
    public String customerBookings(Model model) {
        try {
            System.out.println("[v0] Customer bookings accessed");
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            System.out.println("[v0] Username: " + username);
            
            User customer = userService.findByUsername(username).orElse(null);
            
            if (customer != null) {
                System.out.println("[v0] Customer found: " + customer.getUsername());
                System.out.println("[v0] Customer ID: " + customer.getUserID());
                
                List<Booking> customerBookings = bookingService.getBookingsByCustomer(customer);
                System.out.println("[v0] Found " + customerBookings.size() + " bookings using customer object");
                
                // If no bookings found with customer object, try with customer ID
                if (customerBookings.isEmpty()) {
                    System.out.println("[v0] Trying to find bookings by customer ID: " + customer.getUserID());
                    customerBookings = bookingService.getBookingsByCustomerId(customer.getUserID());
                    System.out.println("[v0] Found " + customerBookings.size() + " bookings using customer ID");
                }
                
                model.addAttribute("bookings", customerBookings);
                
                Map<Long, String> paymentStatusMap = new HashMap<>();
                for (Booking booking : customerBookings) {
                    try {
                        List<PaymentSlip> paymentSlips = invoiceService.getPaymentSlipsByBooking(booking.getBookingID());
                        if (paymentSlips.isEmpty()) {
                            paymentStatusMap.put(booking.getBookingID(), "Payment Not Uploaded");
                        } else {
                            PaymentSlip latestSlip = paymentSlips.get(paymentSlips.size() - 1);
                            String status = latestSlip.getStatus();
                            switch (status) {
                                case "UPLOADED":
                                    paymentStatusMap.put(booking.getBookingID(), "Payment Pending Verification");
                                    break;
                                case "VERIFIED":
                                    paymentStatusMap.put(booking.getBookingID(), "Payment Confirmed");
                                    break;
                                case "REJECTED":
                                    paymentStatusMap.put(booking.getBookingID(), "Payment Rejected");
                                    break;
                                default:
                                    paymentStatusMap.put(booking.getBookingID(), "Payment Not Confirmed");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("[v0] Error processing payment status for booking " + booking.getBookingID() + ": " + e.getMessage());
                        paymentStatusMap.put(booking.getBookingID(), "Payment Status Unknown");
                    }
                }
                model.addAttribute("paymentStatusMap", paymentStatusMap);
            } else {
                System.out.println("[v0] Customer not found");
                model.addAttribute("bookings", new ArrayList<>());
                model.addAttribute("paymentStatusMap", new HashMap<>());
            }
            model.addAttribute("title", "My Bookings");
            return "customer/bookings";
        } catch (Exception e) {
            System.out.println("[v0] Error in customer bookings: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("bookings", new ArrayList<>());
            model.addAttribute("paymentStatusMap", new HashMap<>());
            model.addAttribute("title", "My Bookings");
            model.addAttribute("errorMessage", "Unable to load bookings. Please try again later.");
            return "customer/bookings";
        }
    }
    
    @GetMapping("/bookings/new")
    public String newBookingForm(Model model) {
        model.addAttribute("booking", new Booking());
        model.addAttribute("machines", machineService.getAvailableMachines());
        return "customer/new-booking";
    }
    
    @PostMapping("/bookings/create")
    public String createBooking(@RequestParam Long machineId,
                               @RequestParam String startDate,
                               @RequestParam String endDate,
                               @RequestParam(required = false) String notes,
                               @RequestParam MultipartFile paymentSlip,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User customer = userService.findByUsername(username).orElse(null);
            
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found");
            }
            
            // Validate payment slip
            if (paymentSlip.isEmpty()) {
                throw new IllegalArgumentException("Payment slip is required");
            }
            
            // Get machine
            Optional<Machine> machineOpt = machineService.getMachineById(machineId);
            if (machineOpt.isEmpty()) {
                throw new IllegalArgumentException("Selected machine not found");
            }
            
            // Create booking
            Booking booking = new Booking();
            booking.setCustomer(customer);
            booking.setMachine(machineOpt.get());
            booking.setStartDate(LocalDateTime.parse(startDate));
            booking.setEndDate(LocalDateTime.parse(endDate));
            booking.setNotes(notes);
            booking.setStatus(Booking.BookingStatus.PENDING);
            
            // Calculate total cost if hourly rate is available
            Machine machine = machineOpt.get();
            if (machine.getHourlyRate() != null) {
                long hours = java.time.Duration.between(booking.getStartDate(), booking.getEndDate()).toHours();
                booking.setTotalCost(machine.getHourlyRate().multiply(BigDecimal.valueOf(hours)));
            }
            
            // Save booking
            Booking savedBooking = bookingService.createBooking(booking);
            
            // Handle payment slip upload
            if (!paymentSlip.isEmpty()) {
                String fileName = savePaymentSlip(paymentSlip, savedBooking.getBookingID());
                
                // Create payment slip record
                PaymentSlip slip = new PaymentSlip();
                slip.setBookingId(savedBooking.getBookingID());
                slip.setFilePath(fileName);
                slip.setStatus("UPLOADED");
                invoiceService.savePaymentSlip(slip);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Booking created successfully! Your booking ID is: " + savedBooking.getBookingID());
            return "redirect:/customer/bookings";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/bookings/new";
        }
    }
    
    private String savePaymentSlip(MultipartFile file, Long bookingId) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = "booking_" + bookingId + "_" + System.currentTimeMillis() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        return UPLOAD_DIR + fileName;
    }
    
    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User customer = userService.findByUsername(username).orElse(null);
            
            if (customer != null) {
                bookingService.cancelBooking(id, customer);
                redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customer/bookings";
    }
    
    @PostMapping("/bookings/{id}/delete")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User customer = userService.findByUsername(username).orElse(null);
            
            if (customer != null) {
                bookingService.deleteBooking(id, customer);
                redirectAttributes.addFlashAttribute("successMessage", "Booking deleted successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customer/bookings";
    }
    
    @GetMapping("/machines")
    public String availableMachines(Model model) {
        model.addAttribute("machines", machineService.getAvailableMachines());
        model.addAttribute("title", "Available Machines");
        return "inventory/list";
    }
    
    @GetMapping("/tickets")
    public String supportTickets(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User customer = userService.findByUsername(username).orElse(null);
        
        if (customer != null) {
            model.addAttribute("tickets", ticketService.getTicketsForUser(customer));
        }
        model.addAttribute("title", "My Support Tickets");
        return "customer/tickets";
    }
    
    @GetMapping("/tickets/new")
    public String newTicketForm(Model model) {
        return "customer/new-ticket";
    }
    
    @PostMapping("/tickets/create")
    public String createTicket(@RequestParam String category,
                              @RequestParam String subject,
                              @RequestParam String description,
                              RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User customer = userService.findByUsername(username).orElse(null);
            
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found");
            }
            
            // Create ticket
            Ticket ticket = new Ticket();
            ticket.setCustomer(customer);
            ticket.setSubject(subject);
            ticket.setDescription(description);
            ticket.setCategory(Ticket.TicketCategory.valueOf(category));
            
            Ticket savedTicket = ticketService.createTicket(ticket, customer);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Support ticket created successfully! Your ticket ID is: " + savedTicket.getTicketID());
            return "redirect:/customer/tickets";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customer/tickets/new";
        }
    }
    
    @GetMapping("/browse-machines")
    public String browseMachines(Model model) {
        try {
            System.out.println("[v0] Browse machines accessed");
            List<Machine> machines = machineService.getAvailableMachines();
            System.out.println("[v0] Found " + machines.size() + " available machines");
            model.addAttribute("machines", machines);
            model.addAttribute("title", "Browse Available Machines");
            return "customer/browse-machines";
        } catch (Exception e) {
            System.out.println("[v0] Error in browse machines: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("machines", new ArrayList<>());
            model.addAttribute("title", "Browse Available Machines");
            model.addAttribute("errorMessage", "Unable to load machines. Please try again later.");
            return "customer/browse-machines";
        }
    }
    
    @GetMapping("/bookings/new/{machineId}")
    public String newBookingFormWithMachine(@PathVariable Long machineId, Model model, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("[v0] New booking form accessed for machine: " + machineId);
            Optional<Machine> machineOpt = machineService.getMachineById(machineId);
            if (machineOpt.isEmpty()) {
                System.out.println("[v0] Machine not found: " + machineId);
                redirectAttributes.addFlashAttribute("errorMessage", "Selected machine not found");
                return "redirect:/customer/browse-machines";
            }
            
            model.addAttribute("booking", new Booking());
            model.addAttribute("selectedMachine", machineOpt.get());
            System.out.println("[v0] Machine loaded: " + machineOpt.get().getModel());
            return "customer/new-booking-form";
        } catch (Exception e) {
            System.out.println("[v0] Error in new booking form: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to load booking form. Please try again later.");
            return "redirect:/customer/browse-machines";
        }
    }
}
