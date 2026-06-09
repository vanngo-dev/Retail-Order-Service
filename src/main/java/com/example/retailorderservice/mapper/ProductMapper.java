package com.example.retailorderservice.mapper;

import com.example.retailorderservice.dto.response.ProductResponse;
import com.example.retailorderservice.entity.Product;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantityAvailable(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
