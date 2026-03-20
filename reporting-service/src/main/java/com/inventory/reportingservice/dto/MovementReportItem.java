package com.inventory.reportingservice.dto;

import java.time.LocalDateTime;

public record MovementReportItem(
        Long movementId,
        Long productId,
        String productName,
        String sku,
        String movementType,
        Integer quantity,
        String destination,
        String createdBy,
        LocalDateTime createdAt
) {
}
