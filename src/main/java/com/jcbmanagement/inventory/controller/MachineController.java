package com.jcbmanagement.inventory.controller;

import com.jcbmanagement.inventory.model.Machine;
import com.jcbmanagement.inventory.service.MachineService;
import com.jcbmanagement.booking.repository.BookingRepository;
import com.jcbmanagement.booking.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/inventory")
public class MachineController {
    @Autowired
    private MachineService machineService;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/list")
    public String listMachines(Model model) {
        model.addAttribute("machines", machineService.getAllMachines());
        return "inventory/list";
    }

    @GetMapping("/available")
    public String availableMachines(Model model) {
        model.addAttribute("machines", machineService.getAvailableMachines());
        model.addAttribute("title", "Available Machines (Not Booked)");
        return "inventory/list";
    }

    @GetMapping("/in-use")
    public String inUseMachines(Model model) {
        model.addAttribute("machines", machineService.getMachinesByStatus(Machine.MachineStatus.IN_USE));
        model.addAttribute("title", "Machines In Use");
        return "inventory/list";
    }

    @GetMapping("/maintenance")
    public String maintenanceMachines(Model model) {
        model.addAttribute("machines", machineService.getMachinesByStatus(Machine.MachineStatus.MAINTENANCE));
        model.addAttribute("title", "Machines Under Maintenance");
        return "inventory/list";
    }

    @GetMapping("/{id}")
    public String viewMachine(@PathVariable Long id, Model model) {
        return machineService.getMachineById(id)
                .map(machine -> {
                    model.addAttribute("machine", machine);
                    return "inventory/view";
                })
                .orElse("redirect:/inventory/list");
    }

    @GetMapping("/{id}/edit")
    public String editMachineForm(@PathVariable Long id, Model model) {
        return machineService.getMachineById(id)
                .map(machine -> {
                    model.addAttribute("machine", machine);
                    return "inventory/edit";
                })
                .orElse("redirect:/inventory/list");
    }

    @PostMapping("/{id}/edit")
    public String updateMachine(@PathVariable Long id, @ModelAttribute Machine machine, RedirectAttributes redirectAttributes) {
        try {
            machine.setMachineID(id);
            machineService.updateMachine(machine);
            redirectAttributes.addFlashAttribute("successMessage", "Machine updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/" + id;
    }

    @GetMapping("/new")
    public String newMachineForm(Model model) {
        model.addAttribute("machine", new Machine());
        return "inventory/new";
    }

    @PostMapping("/new")
    public String submitMachine(@Valid @ModelAttribute Machine machine, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "inventory/new";
        }
        try {
            machineService.addMachine(machine);
            redirectAttributes.addFlashAttribute("successMessage", "Machine added successfully!");
            return "redirect:/inventory/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "inventory/new";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteMachine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Machine> machine = machineService.getMachineById(id);
            if (machine.isPresent()) {
                List<Booking> activeBookings = bookingRepository.findConflictingBookings(
                        id, LocalDateTime.now(), LocalDateTime.now().plusDays(365)
                );

                if (!activeBookings.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Cannot delete machine '" + machine.get().getModel() +
                                    "' because it has active bookings. Cancel all bookings first.");
                    return "redirect:/inventory/list";
                }

                machineService.deleteMachine(id);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Machine '" + machine.get().getModel() + "' deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Machine not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting machine: " + e.getMessage());
        }
        return "redirect:/inventory/list";
    }
}
