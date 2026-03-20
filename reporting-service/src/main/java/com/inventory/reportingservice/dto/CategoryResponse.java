package com.inventory.reportingservice.dto;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        boolean active
) {
}
