package com.jcbmanagement.maintenance.repository;

import com.jcbmanagement.maintenance.model.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
}
