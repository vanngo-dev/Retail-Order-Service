package com.example.retailorderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductInactiveException extends RuntimeException {

    public ProductInactiveException(Long productId) {
        super("Product is inactive and cannot be ordered: " + productId);
    }
}
