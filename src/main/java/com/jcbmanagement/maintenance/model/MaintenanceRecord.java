package com.jcbmanagement.maintenance.model;

import com.jcbmanagement.inventory.model.Machine;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "MaintenanceRecords")
public class MaintenanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordID;

    @ManyToOne
    @JoinColumn(name = "MachineID")
    private Machine machine;

    private LocalDateTime maintenanceDate = LocalDateTime.now();
    private String description;

    private LocalDateTime startDate;
    private LocalDateTime endDate; // Completion is implied by this being set

    @Column(name = "CreatedBy", nullable = false)
    private String createdBy;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "NextDueDate")
    private LocalDateTime nextDueDate;
}