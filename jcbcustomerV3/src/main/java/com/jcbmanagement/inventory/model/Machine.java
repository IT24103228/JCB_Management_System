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
    
    @Enumerated(EnumType.STRING)
    private MachineStatus status = MachineStatus.AVAILABLE;
    
    private boolean availability = true;
    
    public enum MachineStatus {
        AVAILABLE, BOOKED, IN_USE, MAINTENANCE, OUT_OF_SERVICE
    }
}
