package com.example.retailorderservice.mapper;

import com.example.retailorderservice.dto.response.OrderItemResponse;
import com.example.retailorderservice.dto.response.OrderResponse;
import com.example.retailorderservice.entity.Order;
import com.example.retailorderservice.entity.OrderItem;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getSubtotal(),
                order.getTax(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                toItemResponses(order.getItems())
        );
    }

    private static List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        return items.stream()
                .map(OrderMapper::toItemResponse)
                .toList();
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getSkuSnapshot(),
                item.getProductNameSnapshot(),
                item.getUnitPriceSnapshot(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }
}
