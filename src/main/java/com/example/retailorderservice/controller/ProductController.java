package com.example.retailorderservice.controller;

import com.example.retailorderservice.dto.request.CreateProductRequest;
import com.example.retailorderservice.dto.request.UpdateProductRequest;
import com.example.retailorderservice.dto.response.ProductResponse;
import com.example.retailorderservice.service.ProductService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ProductResponse> listProducts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String sku,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return productService.listProducts(active, sku, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity
                .created(URI.create("/products/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}
