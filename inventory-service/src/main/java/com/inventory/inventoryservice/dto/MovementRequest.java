package com.inventory.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MovementRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity,
        String reference,
        String destination,
        String notes
) {
}
