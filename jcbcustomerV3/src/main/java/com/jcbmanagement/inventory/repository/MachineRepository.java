package com.jcbmanagement.inventory.repository;

import com.jcbmanagement.inventory.model.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findByAvailabilityTrue();
    
    List<Machine> findByModel(String model);
    
    @Query("SELECT m FROM Machine m WHERE m.availability = true")
    List<Machine> findAvailableMachinesSimple();
    
    @Query(value = "SELECT * FROM Machines WHERE Availability = 1 AND Status = 'Available'", nativeQuery = true)
    List<Machine> findAvailableMachinesNative();
    
    @Query(value = "SELECT * FROM Machines WHERE Status = :status", nativeQuery = true)
    List<Machine> findMachinesByStatusNative(@Param("status") String status);
    
    @Query(value = "SELECT * FROM Machines WHERE Availability = 1", nativeQuery = true)
    List<Machine> findAllAvailableMachinesNative();
    
    List<Machine> findByManufacturer(String manufacturer);

    List<Machine> findByLocation(String location);
}
