package com.example.retailorderservice.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(Long productId, int requestedQuantity, int availableQuantity) {
        super("Insufficient inventory for product id %d: requested %d, available %d"
                .formatted(productId, requestedQuantity, availableQuantity));
    }
}
