package com.jcbmanagement.maintenance.service;

import com.jcbmanagement.maintenance.model.MaintenanceRecord;
import com.jcbmanagement.maintenance.repository.MaintenanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceRecordService {
    @Autowired
    private MaintenanceRecordRepository repository;

    public MaintenanceRecord recordMaintenance(MaintenanceRecord record) {
        return repository.save(record);
    }
}
