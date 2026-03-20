package com.inventory.reportingservice.dto;

public record StockReportItem(
        Long productId,
        String productName,
        String sku,
        String categoryName,
        Integer quantity
) {
}
