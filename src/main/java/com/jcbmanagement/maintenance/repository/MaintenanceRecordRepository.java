package com.jcbmanagement.maintenance.repository;

import com.jcbmanagement.maintenance.model.MaintenanceRecord;
import com.jcbmanagement.inventory.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByMachine(Machine machine);

    // Method to find active or future maintenance records for a given machine
    // An active record is one where endDate is null or in the future
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.machine = :machine AND (mr.endDate IS NULL OR mr.endDate >= :currentDateTime)")
    List<MaintenanceRecord> findActiveOrFutureRecordsByMachine(@Param("machine") Machine machine, @Param("currentDateTime") LocalDateTime currentDateTime);
}