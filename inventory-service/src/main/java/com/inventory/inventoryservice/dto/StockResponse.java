package com.inventory.inventoryservice.dto;

import java.time.LocalDateTime;

public record StockResponse(
        Long productId,
        Integer quantity,
        LocalDateTime updatedAt
) {
}
