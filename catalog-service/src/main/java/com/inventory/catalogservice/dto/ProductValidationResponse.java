package com.inventory.catalogservice.dto;

public record ProductValidationResponse(
        Long id,
        String name,
        String sku,
        Long categoryId,
        boolean active
) {
}
