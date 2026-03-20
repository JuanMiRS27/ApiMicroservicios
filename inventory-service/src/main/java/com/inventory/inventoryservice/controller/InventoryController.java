package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.MovementRequest;
import com.inventory.inventoryservice.dto.MovementResponse;
import com.inventory.inventoryservice.dto.MovementSummaryItem;
import com.inventory.inventoryservice.dto.StockResponse;
import com.inventory.inventoryservice.dto.StockSummaryItem;
import com.inventory.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/api/movements/entries")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<MovementResponse> registerEntry(@Valid @RequestBody MovementRequest request,
                                                          Authentication authentication,
                                                          @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(inventoryService.registerEntry(request, authentication.getName(), authorization));
    }

    @PostMapping("/api/movements/exits")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<MovementResponse> registerExit(@Valid @RequestBody MovementRequest request,
                                                         Authentication authentication,
                                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(inventoryService.registerExit(request, authentication.getName(), authorization));
    }

    @GetMapping("/api/stocks/{productId}")
    public ResponseEntity<StockResponse> getStock(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(inventoryService.getStock(productId));
    }

    @GetMapping("/api/movements")
    public ResponseEntity<List<MovementResponse>> findAllMovements() {
        return ResponseEntity.ok(inventoryService.findAllMovements());
    }

    @GetMapping("/api/internal/stocks-summary")
    public ResponseEntity<List<StockSummaryItem>> stockSummary() {
        return ResponseEntity.ok(inventoryService.stockSummary());
    }

    @GetMapping("/api/internal/movements-summary")
    public ResponseEntity<List<MovementSummaryItem>> movementSummary(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(inventoryService.movementSummary(from, to));
    }
}
