package com.inventory.inventoryservice.dto;

public record StockSummaryItem(
        Long productId,
        Integer quantity
) {
}
