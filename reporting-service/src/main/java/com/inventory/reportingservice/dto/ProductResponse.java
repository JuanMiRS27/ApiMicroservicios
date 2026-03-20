package com.inventory.reportingservice.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        Long categoryId,
        BigDecimal unitPrice,
        boolean active
) {
}
