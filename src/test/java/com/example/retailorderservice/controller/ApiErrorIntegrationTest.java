package com.example.retailorderservice.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiErrorIntegrationTest {

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
    void duplicateSkuReturnsStandardErrorResponse() throws Exception {
        productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, true));

        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(productJson("hammer-001", "Duplicate Hammer", "21.99", 5, true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Product SKU already exists: hammer-001"))
                .andExpect(jsonPath("$.path").value("/products"))
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }

    @Test
    void missingProductNameReturnsValidationErrorResponse() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(productJson("HAMMER-001", "", "19.99", 10, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Name is required"))
                .andExpect(jsonPath("$.path").value("/products"))
                .andExpect(jsonPath("$.validationErrors.name").value("Name is required"));
    }

    @Test
    void zeroPriceReturnsValidationErrorResponse() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(productJson("HAMMER-001", "Steel Hammer", "0.00", 10, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Price must be greater than zero"))
                .andExpect(jsonPath("$.path").value("/products"))
                .andExpect(jsonPath("$.validationErrors.price").value("Price must be greater than zero"));
    }

    @Test
    void invalidOrderIdReturnsNotFoundErrorResponse() throws Exception {
        mockMvc.perform(get("/orders/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/orders/999"));
    }

    @Test
    void insufficientInventoryReturnsConflictErrorResponse() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 1, true));

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(product.getId(), 2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Insufficient inventory for product id %d: requested 2, available 1".formatted(product.getId())))
                .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    void duplicateShipmentReturnsConflictErrorResponse() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, true));
        Long orderId = createOrder(product.getId());

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "1Z999999999")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "1Z999999999")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Order has already been shipped: " + orderId))
                .andExpect(jsonPath("$.path").value("/orders/" + orderId + "/ship"));
    }

    @Test
    void malformedJsonReturnsStandardErrorResponse() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content("{\"sku\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"))
                .andExpect(jsonPath("$.path").value("/products"));
    }

    private Long createOrder(Long productId) throws Exception {
        String location = mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(productId, 1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    private static Product product(String sku, String name, String price, int quantityAvailable, boolean active) {
        return new Product(sku, name, name + " description", new BigDecimal(price), quantityAvailable, active);
    }

    private static String productJson(String sku, String name, String price, int quantityAvailable, boolean active) {
        return """
                {
                  "sku": "%s",
                  "name": "%s",
                  "description": "%s description",
                  "price": %s,
                  "quantityAvailable": %d,
                  "active": %s
                }
                """.formatted(sku, name, name, price, quantityAvailable, active);
    }

    private static String orderJson(Long productId, int quantity) {
        return """
                {
                  "customerEmail": "customer@example.com",
                  "items": [
                    {
                      "productId": %d,
                      "quantity": %d
                    }
                  ]
                }
                """.formatted(productId, quantity);
    }

    private static String shipJson(String carrier, String trackingNumber) {
        return """
                {
                  "carrier": "%s",
                  "trackingNumber": "%s"
                }
                """.formatted(carrier, trackingNumber);
    }
}
