package com.inventory.reportingservice.dto;

import java.time.LocalDateTime;

public record UserAccessLogResponse(
        Long id,
        String username,
        String role,
        String eventType,
        LocalDateTime createdAt
) {
}
