package com.example.retailorderservice.dto.response;

import java.time.Instant;

public record HealthResponse(String status, String service, Instant timestamp) {
}
