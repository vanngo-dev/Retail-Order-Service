package com.example.retailorderservice.exception;

public class ProductInactiveException extends RuntimeException {

    public ProductInactiveException(Long productId) {
        super("Product is inactive and cannot be ordered: " + productId);
    }
}
