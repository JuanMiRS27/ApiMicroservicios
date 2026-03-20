package com.inventory.reportingservice.controller;

import com.inventory.reportingservice.dto.MovementReportResponse;
import com.inventory.reportingservice.dto.ReportHistoryItem;
import com.inventory.reportingservice.dto.StockReportResponse;
import com.inventory.reportingservice.service.ReportingService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/stock-summary")
    public ResponseEntity<StockReportResponse> stockSummary(Authentication authentication,
                                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(reportingService.generateStockReport(authentication.getName(), authorization));
    }

    @GetMapping("/movements-summary")
    public ResponseEntity<MovementReportResponse> movementSummary(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(reportingService.generateMovementReport(from, to, authentication.getName(), authorization));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ReportHistoryItem>> history() {
        return ResponseEntity.ok(reportingService.history());
    }
}
