package com.jcbmanagement.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Machines")
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long machineID;

    @NotBlank(message = "Machine model is required")
    private String model;
    
    private String serialNumber;
    private String manufacturer = "JCB";
    private Integer yearOfManufacture;
    private BigDecimal hourlyRate;
    private String location;
    private String description;
    
    @Column(name = "Status")
    private String statusString;
    
    private boolean availability = true;
    
    @Transient
    public MachineStatus getStatus() {
        if (statusString == null) return MachineStatus.AVAILABLE;
        
        // Map database values to enum values
        switch (statusString.toLowerCase()) {
            case "available": return MachineStatus.AVAILABLE;
            case "inuse": return MachineStatus.IN_USE;
            case "maintenance": return MachineStatus.MAINTENANCE;
            case "booked": return MachineStatus.BOOKED;
            case "out_of_service": return MachineStatus.OUT_OF_SERVICE;
            default:
                // Try direct enum mapping as fallback
                try {
                    return MachineStatus.valueOf(statusString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return MachineStatus.AVAILABLE;
                }
        }
    }
    
    @Transient
    public void setStatus(MachineStatus status) {
        if (status == null) {
            this.statusString = "Available";
            return;
        }
        
        // Map enum values to database values
        switch (status) {
            case AVAILABLE: this.statusString = "Available"; break;
            case IN_USE: this.statusString = "InUse"; break;
            case MAINTENANCE: this.statusString = "Maintenance"; break;
            case BOOKED: this.statusString = "Booked"; break;
            case OUT_OF_SERVICE: this.statusString = "OutOfService"; break;
            default: this.statusString = "Available"; break;
        }
    }
    
    public enum MachineStatus {
        AVAILABLE, BOOKED, IN_USE, MAINTENANCE, OUT_OF_SERVICE
    }
}
