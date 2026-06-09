package com.example.retailorderservice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer quantityAvailable,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
