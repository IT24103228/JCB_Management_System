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
}
