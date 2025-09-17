package com.jcbmanagement.maintenance.controller;

import com.jcbmanagement.maintenance.model.MaintenanceRecord;
import com.jcbmanagement.maintenance.service.MaintenanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceRecordController {
    @Autowired
    private MaintenanceRecordService service;

    @GetMapping("/new")
    public String newRecordForm() {
        return "maintenance/new";
    }

    @PostMapping("/new")
    public String submitRecord(@ModelAttribute MaintenanceRecord record) {
        service.recordMaintenance(record);
        return "redirect:/maintenance/list";
    }
}
