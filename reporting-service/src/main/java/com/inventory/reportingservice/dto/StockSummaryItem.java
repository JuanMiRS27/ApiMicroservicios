package com.inventory.reportingservice.dto;

public record StockSummaryItem(
        Long productId,
        Integer quantity
) {
}
