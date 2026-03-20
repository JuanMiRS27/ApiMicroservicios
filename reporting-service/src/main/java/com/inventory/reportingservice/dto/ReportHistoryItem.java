package com.inventory.reportingservice.dto;

import java.time.LocalDateTime;

public record ReportHistoryItem(
        Long id,
        String reportType,
        String requestedBy,
        String parametersJson,
        LocalDateTime createdAt
) {
}
