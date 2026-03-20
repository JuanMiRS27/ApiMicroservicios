package com.inventory.reportingservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MovementReportResponse(
        LocalDate from,
        LocalDate to,
        LocalDateTime generatedAt,
        Integer totalEntries,
        Integer totalExits,
        List<MovementReportItem> items
) {
}
