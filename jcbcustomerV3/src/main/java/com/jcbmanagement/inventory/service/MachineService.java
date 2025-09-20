package com.jcbmanagement.inventory.service;

import com.jcbmanagement.inventory.model.Machine;
import com.jcbmanagement.inventory.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MachineService {
    @Autowired
    private MachineRepository machineRepository;

    public Machine addMachine(Machine machine) {
        return machineRepository.save(machine);
    }
    
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }
    
    public List<Machine> getAvailableMachines() {
        try {
            System.out.println("[v0] Fetching available machines...");
            
            // Try the most reliable approach first - using availability flag
            List<Machine> availableMachines = machineRepository.findAllAvailableMachinesNative();
            
            if (availableMachines.isEmpty()) {
                System.out.println("[v0] No machines found with native query, trying JPA query...");
                availableMachines = machineRepository.findByAvailabilityTrue();
            }
            
            if (availableMachines.isEmpty()) {
                System.out.println("[v0] No machines found with JPA query, trying status-based query...");
                availableMachines = machineRepository.findAvailableMachinesNative();
            }
            
            System.out.println("[v0] Found " + availableMachines.size() + " available machines");
            
            if (!availableMachines.isEmpty()) {
                Machine firstMachine = availableMachines.get(0);
                System.out.println("[v0] First machine - ID: " + firstMachine.getMachineID() + 
                                 ", Status: " + firstMachine.getStatus() + 
                                 ", Availability: " + firstMachine.isAvailability());
            }
            
            return availableMachines;
        } catch (Exception e) {
            System.out.println("[v0] Error fetching available machines: " + e.getMessage());
            e.printStackTrace();
            
            // Final fallback - return empty list rather than crashing
            return new ArrayList<>();
        }
    }
    
    public Optional<Machine> getMachineById(Long id) {
        return machineRepository.findById(id);
    }
    
    public Machine updateMachine(Machine machine) {
        return machineRepository.save(machine);
    }
    
    public void deleteMachine(Long id) {
        machineRepository.deleteById(id);
    }
    
    public List<Machine> getMachinesByStatus(Machine.MachineStatus status) {
        try {
            // Convert enum to database string format
            String dbStatus;
            switch (status) {
                case AVAILABLE: dbStatus = "Available"; break;
                case IN_USE: dbStatus = "InUse"; break;
                case MAINTENANCE: dbStatus = "Maintenance"; break;
                case BOOKED: dbStatus = "Booked"; break;
                case OUT_OF_SERVICE: dbStatus = "OutOfService"; break;
                default: dbStatus = "Available"; break;
            }
            
            return machineRepository.findMachinesByStatusNative(dbStatus);
        } catch (Exception e) {
            System.out.println("[v0] Error fetching machines by status: " + e.getMessage());
            // Fallback to returning all machines and filtering in Java
            return getAllMachines().stream()
                    .filter(machine -> {
                        try {
                            return machine.getStatus() == status;
                        } catch (Exception ex) {
                            return false;
                        }
                    })
                    .toList();
        }
    }
    
    public Machine updateMachineStatus(Long machineId, Machine.MachineStatus status) {
        Optional<Machine> machineOpt = machineRepository.findById(machineId);
        if (machineOpt.isPresent()) {
            Machine machine = machineOpt.get();
            machine.setStatus(status);
            machine.setAvailability(status == Machine.MachineStatus.AVAILABLE);
            return machineRepository.save(machine);
        }
        throw new IllegalArgumentException("Machine not found with ID: " + machineId);
    }
    
    public List<Machine> getMachinesByModel(String model) {
        return machineRepository.findByModel(model);
    }
    
    public List<Machine> getMachinesByLocation(String location) {
        return machineRepository.findByLocation(location);
    }
}
