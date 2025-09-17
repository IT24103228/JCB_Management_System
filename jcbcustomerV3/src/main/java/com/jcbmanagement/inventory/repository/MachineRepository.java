package com.jcbmanagement.inventory.repository;

import com.jcbmanagement.inventory.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findByAvailabilityTrue();
    
    List<Machine> findByStatus(Machine.MachineStatus status);
    
    List<Machine> findByModel(String model);
    
    @Query("SELECT m FROM Machine m WHERE m.availability = true AND m.status = 'AVAILABLE'")
    List<Machine> findAvailableMachines();
    
    List<Machine> findByManufacturer(String manufacturer);
    
    List<Machine> findByLocation(String location);
}
