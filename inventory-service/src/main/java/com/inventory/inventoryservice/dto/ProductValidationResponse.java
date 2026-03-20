package com.inventory.inventoryservice.dto;

public record ProductValidationResponse(
        Long id,
        String name,
        String sku,
        Long categoryId,
        boolean active
) {
}
