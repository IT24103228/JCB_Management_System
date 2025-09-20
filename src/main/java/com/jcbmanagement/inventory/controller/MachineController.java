package com.jcbmanagement.inventory.controller;

import com.jcbmanagement.inventory.model.Machine;
import com.jcbmanagement.inventory.service.MachineService;
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

@Controller
@RequestMapping("/inventory")
public class MachineController {
    @Autowired
    private MachineService machineService;

    @GetMapping("/list")
    public String listMachines(Model model) {
        model.addAttribute("machines", machineService.getAllMachines());
        return "inventory/list";
    }
    
    @GetMapping("/available")
    public String availableMachines(Model model) {
        model.addAttribute("machines", machineService.getMachinesByStatus(Machine.MachineStatus.AVAILABLE));
        model.addAttribute("title", "Available Machines");
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
}
