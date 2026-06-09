package com.example.retailorderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(Long productId, int requestedQuantity, int availableQuantity) {
        super("Insufficient inventory for product id %d: requested %d, available %d"
                .formatted(productId, requestedQuantity, availableQuantity));
    }
}
