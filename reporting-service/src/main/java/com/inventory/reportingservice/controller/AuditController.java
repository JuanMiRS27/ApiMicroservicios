package com.inventory.reportingservice.controller;

import com.inventory.reportingservice.dto.UserAccessLogResponse;
import com.inventory.reportingservice.service.ReportingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final ReportingService reportingService;

    public AuditController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/access-history")
    public ResponseEntity<List<UserAccessLogResponse>> accessHistory() {
        return ResponseEntity.ok(reportingService.accessHistory());
    }
}
