package com.jcbmanagement.inventory.service;

import com.jcbmanagement.booking.model.Booking;
import com.jcbmanagement.booking.repository.BookingRepository;
import com.jcbmanagement.inventory.model.Machine;
import com.jcbmanagement.inventory.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MachineService {
    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Machine addMachine(Machine machine) {
        return machineRepository.save(machine);
    }

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public List<Machine> getAvailableMachines() {
        try {
            System.out.println("[v0] Fetching truly available machines (excluding booked ones)...");

            // Get all machines that are not booked or in use
            List<Machine> allMachines = machineRepository.findAll();
            List<Machine> availableMachines = new ArrayList<>();

            for (Machine machine : allMachines) {
                // Check if machine has any active bookings
                List<Booking> activeBookings = bookingRepository.findConflictingBookings(
                        machine.getMachineID(),
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(365) // Check for future bookings
                );

                // Machine is available if:
                // 1. Status is AVAILABLE
                // 2. No active bookings exist
                // 3. Availability flag is true
                if (machine.getStatus() == Machine.MachineStatus.AVAILABLE &&
                        activeBookings.isEmpty() &&
                        machine.isAvailability()) {
                    availableMachines.add(machine);
                }
            }

            System.out.println("[v0] Found " + availableMachines.size() + " truly available machines");
            return availableMachines;

        } catch (Exception e) {
            System.out.println("[v0] Error fetching available machines: " + e.getMessage());
            e.printStackTrace();

            // Fallback to basic availability check
            return machineRepository.findByAvailabilityTrue();
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

    public long countTotalMachines() {
        return machineRepository.count();
    }
}
