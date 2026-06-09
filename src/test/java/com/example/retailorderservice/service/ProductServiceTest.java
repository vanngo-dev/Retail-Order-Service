package com.example.retailorderservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.retailorderservice.dto.request.CreateProductRequest;
import com.example.retailorderservice.dto.response.ProductResponse;
import com.example.retailorderservice.entity.Product;
import com.example.retailorderservice.exception.DuplicateSkuException;
import com.example.retailorderservice.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void createsValidProduct() {
        CreateProductRequest request = createRequest("HAMMER-001", "Steel Hammer", "19.99", 100);

        when(productRepository.existsBySkuIgnoreCase("HAMMER-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.createProduct(request);

        assertThat(response.sku()).isEqualTo("HAMMER-001");
        assertThat(response.name()).isEqualTo("Steel Hammer");
        assertThat(response.price()).isEqualByComparingTo("19.99");
        assertThat(response.quantityAvailable()).isEqualTo(100);
        assertThat(response.active()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void rejectsDuplicateSku() {
        CreateProductRequest request = createRequest("HAMMER-001", "Steel Hammer", "19.99", 100);

        when(productRepository.existsBySkuIgnoreCase("HAMMER-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("HAMMER-001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void rejectsNegativeQuantity() {
        CreateProductRequest request = createRequest("HAMMER-001", "Steel Hammer", "19.99", -1);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity available cannot be negative");
        verifyNoInteractions(productRepository);
    }

    @Test
    void rejectsZeroPrice() {
        CreateProductRequest request = createRequest("HAMMER-001", "Steel Hammer", "0.00", 100);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price must be greater than zero");
        verifyNoInteractions(productRepository);
    }

    @Test
    void getsProductById() {
        Product product = new Product("HAMMER-001", "Steel Hammer", "16 oz steel hammer", new BigDecimal("19.99"), 100, true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.sku()).isEqualTo("HAMMER-001");
        assertThat(response.name()).isEqualTo("Steel Hammer");
    }

    @Test
    void deactivatesProduct() {
        Product product = new Product("HAMMER-001", "Steel Hammer", "16 oz steel hammer", new BigDecimal("19.99"), 100, true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deactivateProduct(1L);

        assertThat(product.isActive()).isFalse();
    }

    private static CreateProductRequest createRequest(String sku, String name, String price, int quantityAvailable) {
        return new CreateProductRequest(
                sku,
                name,
                "16 oz steel hammer",
                new BigDecimal(price),
                quantityAvailable,
                true
        );
    }
}
