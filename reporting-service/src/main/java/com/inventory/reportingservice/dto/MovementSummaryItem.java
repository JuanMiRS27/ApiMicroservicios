package com.inventory.reportingservice.dto;

import java.time.LocalDateTime;

public record MovementSummaryItem(
        Long id,
        Long productId,
        String movementType,
        Integer quantity,
        String destination,
        String createdBy,
        LocalDateTime createdAt
) {
}
