package com.example.retailorderservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ShipmentApiIntegrationTest {

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
    void shipsOrderAndUpdatesOrderStatus() throws Exception {
        Long orderId = createOrder();

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "1Z999999999")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/" + orderId + "/shipment"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.carrier").value("UPS"))
                .andExpect(jsonPath("$.trackingNumber").value("1Z999999999"))
                .andExpect(jsonPath("$.shippedAt", notNullValue()))
                .andExpect(jsonPath("$.createdAt", notNullValue()));

        assertThat(shipmentRepository.findByOrder_Id(orderId)).isPresent();
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.SHIPPED);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void rejectsNonexistentOrder() throws Exception {
        mockMvc.perform(post("/orders/{id}/ship", 999L)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "1Z999999999")))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsAlreadyShippedOrder() throws Exception {
        Long orderId = createOrder();
        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "1Z999999999")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "1Z999999999")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsCancelledOrder() throws Exception {
        Long orderId = createOrder();
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.cancel();
        orderRepository.save(order);

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "1Z999999999")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsMissingCarrier() throws Exception {
        Long orderId = createOrder();

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("", "1Z999999999")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingTrackingNumber() throws Exception {
        Long orderId = createOrder();

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .contentType(APPLICATION_JSON)
                        .content(shipJson("UPS", "")))
                .andExpect(status().isBadRequest());
    }

    private Long createOrder() throws Exception {
        Product product = productRepository.save(new Product(
                "HAMMER-001",
                "Steel Hammer",
                "16 oz steel hammer",
                new BigDecimal("19.99"),
                10,
                true
        ));

        String location = mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(product.getId())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/orders/\\d+")))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        assertThat(location).isNotNull();
        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    private static String orderJson(Long productId) {
        return """
                {
                  "customerEmail": "customer@example.com",
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 2
                    }
                  ]
                }
                """.formatted(productId);
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
