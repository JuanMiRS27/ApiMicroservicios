package com.inventory.catalogservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        Long categoryId,
        BigDecimal unitPrice,
        boolean active,
        LocalDateTime createdAt
) {
}
