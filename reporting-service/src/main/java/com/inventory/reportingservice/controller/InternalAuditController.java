package com.inventory.reportingservice.controller;

import com.inventory.reportingservice.dto.AccessEventRequest;
import com.inventory.reportingservice.dto.UserAccessLogResponse;
import com.inventory.reportingservice.service.ReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
public class InternalAuditController {

    private final ReportingService reportingService;

    public InternalAuditController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @PostMapping("/access-events")
    public ResponseEntity<UserAccessLogResponse> registerAccessEvent(@RequestBody AccessEventRequest request) {
        return ResponseEntity.ok(reportingService.registerAccessEvent(request));
    }
}
