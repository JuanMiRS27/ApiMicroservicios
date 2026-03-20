package com.inventory.authservice.dto;

public record AccessEventRequest(
        String username,
        String role,
        String eventType
) {
}
