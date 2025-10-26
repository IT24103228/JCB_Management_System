package com.jcbmanagement.inventory.service;

import com.jcbmanagement.booking.model.Booking;
import com.jcbmanagement.booking.repository.BookingRepository;
import com.jcbmanagement.inventory.model.Machine;
import com.jcbmanagement.inventory.repository.MachineRepository;
import com.jcbmanagement.maintenance.repository.MaintenanceRecordRepository; // Import MaintenanceRecordRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MachineService {
    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository; // Inject MaintenanceRecordRepository

    public Machine addMachine(Machine machine) {
        return machineRepository.save(machine);
    }

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public List<Machine> getAvailableMachines() {
        try {
            List<Machine> allMachines = machineRepository.findAll();
            List<Machine> availableMachines = new ArrayList<>();

            for (Machine machine : allMachines) {
                // Check for active (non-cancelled) bookings
                List<Booking> activeBookings = bookingRepository.findConflictingBookings(
                        machine.getMachineID(),
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(365)
                );

                // Machine is available if:
                // 1. Status is AVAILABLE
                // 2. No active bookings exist (cancelled bookings are excluded by findConflictingBookings)
                // 3. Availability flag is true
                if (machine.getStatus() == Machine.MachineStatus.AVAILABLE &&
                        activeBookings.isEmpty() &&
                        machine.isAvailability()) {
                    availableMachines.add(machine);
                }
            }

            return availableMachines;

        } catch (Exception e) {
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
        // Check if there are any booking records for this machine
        List<Booking> machineBookings = bookingRepository.findByMachineMachineID(id);

        if (!machineBookings.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete machine. There are " + machineBookings.size() +
                            " booking record(s) associated with this machine. " +
                            "Please ask the booking manager to delete all booking records first."
            );
        }

        machineRepository.deleteById(id);
    }

    public List<Machine> getMachinesByStatus(Machine.MachineStatus status) {
        try {
            String dbStatus;
            switch (status) {
                case AVAILABLE: dbStatus = "Available"; break;
                case MAINTENANCE: dbStatus = "Maintenance"; break;
                case BOOKED: dbStatus = "Booked"; break;
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

    public List<Machine> getBookedMachines() {
        try {
            List<Machine> allMachines = machineRepository.findAll();
            List<Machine> bookedMachines = new ArrayList<>();

            for (Machine machine : allMachines) {
                List<Booking> activeBookings = bookingRepository.findConflictingBookings(
                        machine.getMachineID(),
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(365)
                );

                // Machine is booked if it has active bookings OR status is BOOKED
                if (!activeBookings.isEmpty() || machine.getStatus() == Machine.MachineStatus.BOOKED) {
                    bookedMachines.add(machine);
                }
            }

            return bookedMachines;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Returns a list of machines that are in 'MAINTENANCE' status but do not yet have an associated
     * maintenance record, indicating they are "alerts" needing attention.
     */
    public List<Machine> getMachinesNeedingAttention() {
        List<Machine> maintenanceMachines = getMachinesByStatus(Machine.MachineStatus.MAINTENANCE);
        return maintenanceMachines.stream()
                .filter(machine -> maintenanceRecordRepository.findByMachine(machine).isEmpty())
                .collect(Collectors.toList());
    }
}