package com.inventory.reportingservice.dto;

public record AccessEventRequest(
        String username,
        String role,
        String eventType
) {
}
