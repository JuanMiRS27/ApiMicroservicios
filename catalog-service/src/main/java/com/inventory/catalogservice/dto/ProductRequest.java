package com.inventory.catalogservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @NotNull Long categoryId,
        @NotNull @DecimalMin("0.0") BigDecimal unitPrice
) {
}
