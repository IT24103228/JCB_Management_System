package com.jcbmanagement.inventory.service;

import com.jcbmanagement.inventory.model.Machine;
import com.jcbmanagement.inventory.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MachineService {
    @Autowired
    private MachineRepository machineRepository;

    public Machine addMachine(Machine machine) {
        return machineRepository.save(machine);
    }
    
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }
    
    public List<Machine> getAvailableMachines() {
        return machineRepository.findAvailableMachines();
    }
    
    public Optional<Machine> getMachineById(Long id) {
        return machineRepository.findById(id);
    }
    
    public Machine updateMachine(Machine machine) {
        return machineRepository.save(machine);
    }
    
    public void deleteMachine(Long id) {
        machineRepository.deleteById(id);
    }
    
    public Machine updateMachineStatus(Long machineId, Machine.MachineStatus status) {
        Optional<Machine> machineOpt = machineRepository.findById(machineId);
        if (machineOpt.isPresent()) {
            Machine machine = machineOpt.get();
            machine.setStatus(status);
            machine.setAvailability(status == Machine.MachineStatus.AVAILABLE);
            return machineRepository.save(machine);
        }
        throw new IllegalArgumentException("Machine not found with ID: " + machineId);
    }
    
    public List<Machine> getMachinesByStatus(Machine.MachineStatus status) {
        return machineRepository.findByStatus(status);
    }
    
    public List<Machine> getMachinesByModel(String model) {
        return machineRepository.findByModel(model);
    }
    
    public List<Machine> getMachinesByLocation(String location) {
        return machineRepository.findByLocation(location);
    }
}
