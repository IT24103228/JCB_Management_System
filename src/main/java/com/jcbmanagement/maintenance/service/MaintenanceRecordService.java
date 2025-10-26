package com.jcbmanagement.maintenance.service;

import com.jcbmanagement.maintenance.model.MaintenanceRecord;
import com.jcbmanagement.maintenance.repository.MaintenanceRecordRepository;
import com.jcbmanagement.inventory.model.Machine;
import com.jcbmanagement.inventory.service.MachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceRecordService {
    @Autowired
    private MaintenanceRecordRepository repository;

    @Autowired
    private MachineService machineService;

    @Transactional
    public MaintenanceRecord recordMaintenance(MaintenanceRecord record, String createdBy) {
        record.setCreatedBy(createdBy);
        // No explicit status field, completion is determined by endDate
        return repository.save(record);
    }

    @Transactional
    public MaintenanceRecord recordMaintenance(MaintenanceRecord record) {
        if (record.getCreatedBy() == null || record.getCreatedBy().isEmpty()) {
            record.setCreatedBy("system"); // Default fallback
        }
        // No explicit status field, completion is determined by endDate
        return repository.save(record);
    }

    public List<MaintenanceRecord> getAllMaintenanceRecords() {
        List<MaintenanceRecord> records = repository.findAll();
        System.out.println("[v0] Service retrieved " + records.size() + " records from repository");
        return records;
    }

    @Transactional
    public void deleteMaintenanceRecord(Long recordId) {
        Optional<MaintenanceRecord> recordOpt = repository.findById(recordId);
        if (recordOpt.isPresent()) {
            MaintenanceRecord record = recordOpt.get();
            // Only allow deletion if the record is completed (endDate is set and in the past)
            if (record.getEndDate() == null || record.getEndDate().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Only completed maintenance records can be deleted. Please mark the record as completed first.");
            }
            repository.deleteById(recordId);
        } else {
            throw new IllegalArgumentException("Maintenance record not found with ID: " + recordId);
        }
    }

    @Transactional
    public MaintenanceRecord completeMaintenance(Long recordId) {
        Optional<MaintenanceRecord> recordOpt = repository.findById(recordId);
        if (recordOpt.isPresent()) {
            MaintenanceRecord record = recordOpt.get();
            record.setEndDate(LocalDateTime.now()); // Mark as completed now
            repository.save(record);

            // Update the machine status back to AVAILABLE if no other ongoing maintenance records exist
            Machine machine = record.getMachine();
            if (machine != null) {
                List<MaintenanceRecord> ongoingRecords = repository.findActiveOrFutureRecordsByMachine(machine, LocalDateTime.now());
                if (ongoingRecords.isEmpty()) {
                    // No other active maintenance records, so set machine to AVAILABLE
                    machine.setStatus(Machine.MachineStatus.AVAILABLE);
                    machine.setAvailability(true);
                    machineService.updateMachine(machine);
                }
            }
            return record;
        }
        throw new IllegalArgumentException("Maintenance record not found with ID: " + recordId);
    }
}