package com.example.retailorderservice.service;

import com.example.retailorderservice.dto.request.CreateProductRequest;
import com.example.retailorderservice.dto.request.UpdateProductRequest;
import com.example.retailorderservice.dto.response.ProductResponse;
import com.example.retailorderservice.entity.Product;
import com.example.retailorderservice.exception.DuplicateSkuException;
import com.example.retailorderservice.exception.ProductNotFoundException;
import com.example.retailorderservice.mapper.ProductMapper;
import com.example.retailorderservice.repository.ProductRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(Boolean active, String sku, Pageable pageable) {
        String normalizedSku = normalize(sku);

        if (StringUtils.hasText(normalizedSku) && active != null) {
            return productRepository.findBySkuIgnoreCaseAndActive(normalizedSku, active, pageable)
                    .map(ProductMapper::toResponse);
        }

        if (StringUtils.hasText(normalizedSku)) {
            return productRepository.findBySkuIgnoreCase(normalizedSku, pageable)
                    .map(ProductMapper::toResponse);
        }

        if (active != null) {
            return productRepository.findByActive(active, pageable)
                    .map(ProductMapper::toResponse);
        }

        return productRepository.findAll(pageable).map(ProductMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return ProductMapper.toResponse(findProduct(id));
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        String sku = normalizeRequired(request.sku(), "SKU is required");
        validatePriceAndQuantity(request.price(), request.quantityAvailable());

        if (productRepository.existsBySkuIgnoreCase(sku)) {
            LOGGER.warn("Duplicate product SKU rejected: sku={}", sku);
            throw new DuplicateSkuException(sku);
        }

        Product product = new Product(
                sku,
                normalizeRequired(request.name(), "Name is required"),
                normalize(request.description()),
                request.price(),
                request.quantityAvailable(),
                request.active() == null || request.active()
        );

        Product savedProduct = productRepository.save(product);
        LOGGER.info(
                "Product created: productId={}, sku={}, active={}, quantityAvailable={}",
                savedProduct.getId(),
                savedProduct.getSku(),
                savedProduct.isActive(),
                savedProduct.getQuantityAvailable()
        );
        return ProductMapper.toResponse(savedProduct);
    }

    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = findProduct(id);
        String sku = normalizeRequired(request.sku(), "SKU is required");
        validatePriceAndQuantity(request.price(), request.quantityAvailable());

        if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
            LOGGER.warn("Duplicate product SKU rejected during update: productId={}, sku={}", id, sku);
            throw new DuplicateSkuException(sku);
        }

        product.updateDetails(
                sku,
                normalizeRequired(request.name(), "Name is required"),
                normalize(request.description()),
                request.price(),
                request.quantityAvailable(),
                request.active()
        );

        return ProductMapper.toResponse(product);
    }

    public void deactivateProduct(Long id) {
        findProduct(id).deactivate();
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private static void validatePriceAndQuantity(BigDecimal price, Integer quantityAvailable) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (quantityAvailable == null || quantityAvailable < 0) {
            throw new IllegalArgumentException("Quantity available cannot be negative");
        }
    }

    private static String normalizeRequired(String value, String message) {
        String normalized = normalize(value);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
