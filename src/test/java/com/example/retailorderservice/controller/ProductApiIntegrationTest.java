package com.example.retailorderservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.retailorderservice.entity.Product;
import com.example.retailorderservice.repository.OrderItemRepository;
import com.example.retailorderservice.repository.OrderRepository;
import com.example.retailorderservice.repository.ProductRepository;
import com.example.retailorderservice.repository.ShipmentRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @BeforeEach
    void cleanDatabase() {
        shipmentRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void createsProduct() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(validProductJson("HAMMER-001", "Steel Hammer", "19.99", 100, true)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/products/\\d+")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.sku").value("HAMMER-001"))
                .andExpect(jsonPath("$.name").value("Steel Hammer"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.quantityAvailable").value(100))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void listsProductsWithActiveFilter() throws Exception {
        productRepository.save(new Product("HAMMER-001", "Steel Hammer", "16 oz steel hammer", new BigDecimal("19.99"), 100, true));
        productRepository.save(new Product("OLD-SAW-001", "Old Saw", "Discontinued saw", new BigDecimal("9.99"), 0, false));

        mockMvc.perform(get("/products").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("HAMMER-001"))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    @Test
    void listsProductsWithSkuFilterAndPagination() throws Exception {
        productRepository.save(new Product("HAMMER-001", "Steel Hammer", "16 oz steel hammer", new BigDecimal("19.99"), 100, true));
        productRepository.save(new Product("SAW-001", "Hand Saw", "15 inch hand saw", new BigDecimal("14.99"), 25, true));

        mockMvc.perform(get("/products")
                        .param("sku", "hammer-001")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("HAMMER-001"));
    }

    @Test
    void getsProductById() throws Exception {
        Product product = productRepository.save(
                new Product("HAMMER-001", "Steel Hammer", "16 oz steel hammer", new BigDecimal("19.99"), 100, true)
        );

        mockMvc.perform(get("/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.sku").value("HAMMER-001"));
    }

    @Test
    void updatesProduct() throws Exception {
        Product product = productRepository.save(
                new Product("HAMMER-001", "Steel Hammer", "16 oz steel hammer", new BigDecimal("19.99"), 100, true)
        );

        mockMvc.perform(put("/products/{id}", product.getId())
                        .contentType(APPLICATION_JSON)
                        .content(validProductJson("HAMMER-002", "Steel Hammer Pro", "20 oz steel hammer", 24.99, 50, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("HAMMER-002"))
                .andExpect(jsonPath("$.name").value("Steel Hammer Pro"))
                .andExpect(jsonPath("$.price").value(24.99))
                .andExpect(jsonPath("$.quantityAvailable").value(50))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void deactivatesProductInsteadOfDeletingIt() throws Exception {
        Product product = productRepository.save(
                new Product("HAMMER-001", "Steel Hammer", "16 oz steel hammer", new BigDecimal("19.99"), 100, true)
        );

        mockMvc.perform(delete("/products/{id}", product.getId()))
                .andExpect(status().isNoContent());

        Product deactivatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(deactivatedProduct.isActive()).isFalse();
    }

    @Test
    void rejectsDuplicateSku() throws Exception {
        productRepository.save(new Product("HAMMER-001", "Steel Hammer", "16 oz steel hammer", new BigDecimal("19.99"), 100, true));

        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(validProductJson("hammer-001", "Second Hammer", "Duplicate SKU test", "21.99", 10, true)))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsDuplicateSkuOnUpdate() throws Exception {
        productRepository.save(new Product("HAMMER-001", "Steel Hammer", "16 oz steel hammer", new BigDecimal("19.99"), 100, true));
        Product product = productRepository.save(
                new Product("SAW-001", "Hand Saw", "15 inch hand saw", new BigDecimal("14.99"), 25, true)
        );

        mockMvc.perform(put("/products/{id}", product.getId())
                        .contentType(APPLICATION_JSON)
                        .content(validProductJson("hammer-001", "Hand Saw", "15 inch hand saw", "14.99", 25, true)))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidProductPayload() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(validProductJson("HAMMER-001", "", "19.99", 100, true)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsZeroPricePayload() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(validProductJson("HAMMER-001", "Steel Hammer", "0.00", 100, true)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsNegativeQuantityPayload() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(validProductJson("HAMMER-001", "Steel Hammer", "19.99", -1, true)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownProduct() throws Exception {
        mockMvc.perform(get("/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    private static String validProductJson(
            String sku,
            String name,
            String price,
            int quantityAvailable,
            boolean active
    ) {
        return validProductJson(sku, name, "16 oz steel hammer", price, quantityAvailable, active);
    }

    private static String validProductJson(
            String sku,
            String name,
            String description,
            String price,
            int quantityAvailable,
            boolean active
    ) {
        return """
                {
                  "sku": "%s",
                  "name": "%s",
                  "description": "%s",
                  "price": %s,
                  "quantityAvailable": %d,
                  "active": %s
                }
                """.formatted(sku, name, description, price, quantityAvailable, active);
    }

    private static String validProductJson(
            String sku,
            String name,
            String description,
            double price,
            int quantityAvailable,
            boolean active
    ) {
        return validProductJson(sku, name, description, String.valueOf(price), quantityAvailable, active);
    }
}
