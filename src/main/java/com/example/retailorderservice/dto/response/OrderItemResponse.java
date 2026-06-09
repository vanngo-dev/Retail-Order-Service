package com.example.retailorderservice.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String skuSnapshot,
        String productNameSnapshot,
        BigDecimal unitPriceSnapshot,
        Integer quantity,
        BigDecimal lineTotal
) {
}
