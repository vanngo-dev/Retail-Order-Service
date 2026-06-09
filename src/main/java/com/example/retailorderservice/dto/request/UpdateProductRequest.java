package com.example.retailorderservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank(message = "SKU is required")
        @Size(max = 64, message = "SKU must be 64 characters or fewer")
        String sku,

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be 255 characters or fewer")
        String name,

        @Size(max = 1000, message = "Description must be 1000 characters or fewer")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        BigDecimal price,

        @NotNull(message = "Quantity available is required")
        @Min(value = 0, message = "Quantity available cannot be negative")
        Integer quantityAvailable,

        Boolean active
) {
}
