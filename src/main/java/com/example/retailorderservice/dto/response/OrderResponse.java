package com.example.retailorderservice.dto.response;

import com.example.retailorderservice.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        String customerEmail,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResponse> items
) {
}
