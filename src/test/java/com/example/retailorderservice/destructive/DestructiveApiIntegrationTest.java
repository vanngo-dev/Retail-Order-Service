package com.example.retailorderservice.destructive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.retailorderservice.entity.Order;
import com.example.retailorderservice.entity.OrderStatus;
import com.example.retailorderservice.entity.Product;
import com.example.retailorderservice.repository.OrderItemRepository;
import com.example.retailorderservice.repository.OrderRepository;
import com.example.retailorderservice.repository.ProductRepository;
import com.example.retailorderservice.repository.ShipmentRepository;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DestructiveApiIntegrationTest {

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
    void malformedJsonReturnsBadRequestAndDoesNotPersistProduct() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content("{\"sku\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"))
                .andExpect(jsonPath("$.path").value("/products"));

        assertThat(productRepository.count()).isZero();
    }

    @ParameterizedTest
    @MethodSource("invalidProductPayloads")
    void invalidProductPayloadsReturnBadRequestAndDoNotPersistProduct(
            String payload,
            String field,
            String message
    ) throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$['validationErrors']['" + field + "']").value(message));

        assertThat(productRepository.count()).isZero();
    }

    @Test
    void duplicateSkuReturnsConflictAndKeepsOnlyOriginalProduct() throws Exception {
        productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, true));

        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(productJson("hammer-001", "Duplicate Hammer", "21.99", 5, true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Product SKU already exists: hammer-001"))
                .andExpect(jsonPath("$.path").value("/products"));

        assertThat(productRepository.count()).isEqualTo(1);
        Product product = productRepository.findBySkuIgnoreCase("HAMMER-001").orElseThrow();
        assertThat(product.getName()).isEqualTo("Steel Hammer");
        assertThat(product.getQuantityAvailable()).isEqualTo(10);
    }

    @ParameterizedTest
    @MethodSource("invalidOrderPayloads")
    void invalidOrderPayloadsReturnBadRequestAndDoNotCreateOrder(
            String payload,
            String field,
            String message
    ) throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$['validationErrors']['" + field + "']").value(message));

        assertThat(orderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
        assertThat(shipmentRepository.count()).isZero();
    }

    @Test
    void nonexistentProductOrderReturnsNotFoundAndDoesNotCreateOrder() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(999L, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/orders"));

        assertThat(orderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
    }

    @Test
    void insufficientInventoryReturnsConflictAndDoesNotDeductInventory() throws Exception {
        Product product = productRepository.save(product("SAW-001", "Hand Saw", "14.99", 1, true));

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(product.getId(), 2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Insufficient inventory for product id %d: requested 2, available 1".formatted(product.getId())))
                .andExpect(jsonPath("$.path").value("/orders"));

        assertThat(productRepository.findById(product.getId()).orElseThrow().getQuantityAvailable()).isEqualTo(1);
        assertThat(orderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
    }

    @Test
    void inactiveProductOrderReturnsConflictAndDoesNotCreateOrder() throws Exception {
        Product product = productRepository.save(product("DRILL-001", "Cordless Drill", "89.99", 5, false));

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(product.getId(), 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Product is inactive and cannot be ordered: " + product.getId()))
                .andExpect(jsonPath("$.path").value("/orders"));

        Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(unchangedProduct.isActive()).isFalse();
        assertThat(unchangedProduct.getQuantityAvailable()).isEqualTo(5);
        assertThat(orderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
    }

    @Test
    void invalidPathIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/products/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid value for 'id': not-a-number"))
                .andExpect(jsonPath("$.path").value("/products/not-a-number"));
    }

    @ParameterizedTest
    @MethodSource("invalidShipmentPayloads")
    void invalidShipmentPayloadsReturnBadRequestAndDoNotCreateShipment(
            String payload,
            String field,
            String message
    ) throws Exception {
        Long orderId = createOrderWithProduct();

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$['validationErrors']['" + field + "']").value(message));

        assertThat(shipmentRepository.count()).isZero();
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void nonexistentOrderShipmentReturnsNotFoundAndDoesNotCreateShipment() throws Exception {
        mockMvc.perform(post("/orders/{id}/ship", 999L)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "1Z999999999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/orders/999/ship"));

        assertThat(shipmentRepository.count()).isZero();
    }

    @Test
    void duplicateShipmentReturnsConflictAndKeepsOriginalShipment() throws Exception {
        Long orderId = createOrderWithProduct();

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "1Z999999999")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("FedEx", "999999999999")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Order has already been shipped: " + orderId))
                .andExpect(jsonPath("$.path").value("/orders/" + orderId + "/ship"));

        assertThat(shipmentRepository.count()).isEqualTo(1);
        assertThat(shipmentRepository.findByOrder_Id(orderId).orElseThrow().getCarrier()).isEqualTo("UPS");
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void cancelledOrderShipmentReturnsConflictAndDoesNotCreateShipment() throws Exception {
        Long orderId = createOrderWithProduct();
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.cancel();
        orderRepository.save(order);

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "1Z999999999")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Cancelled order cannot be shipped: " + orderId))
                .andExpect(jsonPath("$.path").value("/orders/" + orderId + "/ship"));

        assertThat(shipmentRepository.count()).isZero();
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    private Long createOrderWithProduct() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, true));
        String location = mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(product.getId(), 1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    private static Stream<Arguments> invalidProductPayloads() {
        return Stream.of(
                Arguments.of("""
                        {
                          "name": "Steel Hammer",
                          "description": "Missing SKU",
                          "price": 19.99,
                          "quantityAvailable": 10,
                          "active": true
                        }
                        """, "sku", "SKU is required"),
                Arguments.of(productJson("HAMMER-001", "", "19.99", 10, true), "name", "Name is required"),
                Arguments.of(productJson("HAMMER-001", "Steel Hammer", "0.00", 10, true), "price", "Price must be greater than zero"),
                Arguments.of(productJson("HAMMER-001", "Steel Hammer", "-1.00", 10, true), "price", "Price must be greater than zero"),
                Arguments.of(productJson("HAMMER-001", "Steel Hammer", "19.99", -1, true), "quantityAvailable", "Quantity available cannot be negative")
        );
    }

    private static Stream<Arguments> invalidOrderPayloads() {
        return Stream.of(
                Arguments.of("""
                        {
                          "customerEmail": null,
                          "items": [
                            {
                              "productId": 1,
                              "quantity": 1
                            }
                          ]
                        }
                        """, "customerEmail", "Customer email is required"),
                Arguments.of("""
                        {
                          "customerEmail": "not-an-email",
                          "items": [
                            {
                              "productId": 1,
                              "quantity": 1
                            }
                          ]
                        }
                        """, "customerEmail", "Customer email must be valid"),
                Arguments.of("""
                        {
                          "customerEmail": "customer@example.com",
                          "items": []
                        }
                        """, "items", "Order must have at least one item"),
                Arguments.of(orderJson(1L, 0), "items[0].quantity", "Quantity must be greater than zero")
        );
    }

    private static Stream<Arguments> invalidShipmentPayloads() {
        return Stream.of(
                Arguments.of("""
                        {
                          "trackingNumber": "1Z999999999"
                        }
                        """, "carrier", "Carrier is required"),
                Arguments.of("""
                        {
                          "carrier": "UPS"
                        }
                        """, "trackingNumber", "Tracking number is required")
        );
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
