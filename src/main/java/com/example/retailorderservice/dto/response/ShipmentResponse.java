package com.example.retailorderservice.dto.response;

import java.time.Instant;

public record ShipmentResponse(
        Long id,
        Long orderId,
        String carrier,
        String trackingNumber,
        Instant shippedAt,
        Instant createdAt
) {
}
