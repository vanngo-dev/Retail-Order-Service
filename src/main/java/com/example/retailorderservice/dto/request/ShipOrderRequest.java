package com.example.retailorderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShipOrderRequest(
        @NotBlank(message = "Carrier is required")
        @Size(max = 100, message = "Carrier must be 100 characters or fewer")
        String carrier,

        @NotBlank(message = "Tracking number is required")
        @Size(max = 100, message = "Tracking number must be 100 characters or fewer")
        String trackingNumber
) {
}
