package com.inventory.reportingservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StockReportResponse(
        LocalDateTime generatedAt,
        List<StockReportItem> items
) {
}
