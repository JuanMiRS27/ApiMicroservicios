package com.inventory.inventoryservice.dto;

import com.inventory.inventoryservice.entity.MovementType;
import java.time.LocalDateTime;

public record MovementResponse(
        Long id,
        Long productId,
        MovementType movementType,
        Integer quantity,
        String reference,
        String destination,
        String notes,
        String createdBy,
        LocalDateTime createdAt
) {
}
