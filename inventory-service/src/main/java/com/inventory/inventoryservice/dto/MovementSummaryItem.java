package com.inventory.inventoryservice.dto;

import com.inventory.inventoryservice.entity.MovementType;
import java.time.LocalDateTime;

public record MovementSummaryItem(
        Long id,
        Long productId,
        MovementType movementType,
        Integer quantity,
        String destination,
        String createdBy,
        LocalDateTime createdAt
) {
}
