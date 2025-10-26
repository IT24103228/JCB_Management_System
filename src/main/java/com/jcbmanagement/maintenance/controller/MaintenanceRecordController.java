package com.jcbmanagement.maintenance.controller;

import com.jcbmanagement.maintenance.model.MaintenanceRecord;
import com.jcbmanagement.maintenance.service.MaintenanceRecordService;
import com.jcbmanagement.inventory.service.MachineService;
import com.jcbmanagement.inventory.model.Machine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceRecordController {
    @Autowired
    private MaintenanceRecordService service;

    @Autowired
    private MachineService machineService;

    @GetMapping("/new")
    public String newRecordForm() {
        return "maintenance/new";
    }

    @PostMapping("/new")
    public String submitRecord(@ModelAttribute MaintenanceRecord record, HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        if (currentUser == null) {
            currentUser = "admin"; // Default fallback
        }
        service.recordMaintenance(record, currentUser);
        return "redirect:/maintenance/list";
    }

    @GetMapping("/available")
    public String showAvailableMachines(Model model) {
        model.addAttribute("availableMachines", machineService.getAvailableMachines());
        return "maintenance/available";
    }

    @GetMapping("/alerts")
    public String showMaintenanceAlerts(Model model) {
        // Fetch machines that are in MAINTENANCE status but do not yet have an active/future maintenance record
        List<Machine> machinesNeedingAttention = machineService.getMachinesNeedingAttention();
        model.addAttribute("alertMachines", machinesNeedingAttention);
        return "maintenance/alerts";
    }

    @GetMapping("/add-repair/{machineId}")
    public String showAddRepairForm(@PathVariable Long machineId, Model model) {
        Optional<Machine> machine = machineService.getMachineById(machineId);
        if (machine.isPresent()) {
            model.addAttribute("machine", machine.get());
            model.addAttribute("maintenanceRecord", new MaintenanceRecord());
            return "maintenance/add-repair";
        }
        return "redirect:/maintenance/alerts"; // Redirect to alerts if machine not found
    }

    @PostMapping("/add-repair")
    public String addToRepair(@ModelAttribute MaintenanceRecord record,
                              @RequestParam Long machineId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              Model model) { // Added Model to pass errors back to the form
        try {
            // Server-side validation for dates
            if (record.getStartDate() == null || record.getEndDate() == null) {
                throw new IllegalArgumentException("Start Date and Expected End Date are required.");
            }
            if (record.getEndDate().isBefore(record.getStartDate())) {
                throw new IllegalArgumentException("Expected End Date cannot be before Start Date.");
            }
            if (record.getStartDate().isBefore(LocalDateTime.now().minusMinutes(1))) { // Allow a small buffer for current time
                throw new IllegalArgumentException("Start Date cannot be in the past.");
            }

            Optional<Machine> machineOpt = machineService.getMachineById(machineId);
            if (machineOpt.isPresent()) {
                Machine machine = machineOpt.get();

                // Set machine to maintenance status (if not already)
                if (machine.getStatus() != Machine.MachineStatus.MAINTENANCE) {
                    machine.setStatus(Machine.MachineStatus.MAINTENANCE);
                    machine.setAvailability(false);
                    machineService.updateMachine(machine);
                }

                // Create maintenance record
                record.setMachine(machine);
                record.setMaintenanceDate(LocalDateTime.now());

                String currentUser = (String) session.getAttribute("username");
                if (currentUser == null) {
                    currentUser = "admin"; // Default fallback
                }

                service.recordMaintenance(record, currentUser);

                redirectAttributes.addFlashAttribute("success", "Machine added to repair successfully!");
            } else {
                throw new IllegalArgumentException("Machine not found.");
            }
        } catch (IllegalArgumentException e) {
            // If validation fails, add error message and return to the form
            model.addAttribute("errorMessage", e.getMessage());
            Optional<Machine> machine = machineService.getMachineById(machineId);
            machine.ifPresent(value -> model.addAttribute("machine", value));
            model.addAttribute("maintenanceRecord", record); // Keep user's input
            return "maintenance/add-repair";
        } catch (Exception e) {
            System.out.println("[v0] Error adding machine to repair: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to add machine to repair: " + e.getMessage());
        }
        return "redirect:/maintenance/history"; // Redirect to history after adding to repair
    }

    @GetMapping("/history")
    public String showMaintenanceHistory(Model model) {
        List<MaintenanceRecord> records = service.getAllMaintenanceRecords();
        System.out.println("[v0] Retrieved " + records.size() + " maintenance records");
        for (MaintenanceRecord record : records) {
            System.out.println("[v0] Record ID: " + record.getRecordID() + ", Description: " + record.getDescription());
        }
        model.addAttribute("maintenanceRecords", records);
        model.addAttribute("currentDateTime", LocalDateTime.now()); // Add current time for comparison in Thymeleaf
        return "maintenance/history";
    }

    @PostMapping("/delete/{recordId}")
    public String deleteMaintenanceRecord(@PathVariable Long recordId, RedirectAttributes redirectAttributes) {
        try {
            service.deleteMaintenanceRecord(recordId);
            redirectAttributes.addFlashAttribute("success", "Maintenance record deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage()); // Display validation error
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete maintenance record: " + e.getMessage());
        }
        return "redirect:/maintenance/history";
    }

    @PostMapping("/complete/{recordId}")
    public String completeMaintenanceRecord(@PathVariable Long recordId, RedirectAttributes redirectAttributes) {
        try {
            service.completeMaintenance(recordId);
            redirectAttributes.addFlashAttribute("success", "Maintenance record marked as completed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to complete maintenance record: " + e.getMessage());
        }
        return "redirect:/maintenance/history";
    }

    @GetMapping("/status")
    public String showMachineStatus(Model model) {
        try {
            List<Machine> allMachines = machineService.getAllMachines();
            model.addAttribute("machines", allMachines);
            model.addAttribute("currentDateTime", LocalDateTime.now());
            return "maintenance/status";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading machine statuses: " + e.getMessage());
            model.addAttribute("machines", List.of());
            return "maintenance/status";
        }
    }
    // Removed the @PostMapping("/status/{machineId}/update") method
}