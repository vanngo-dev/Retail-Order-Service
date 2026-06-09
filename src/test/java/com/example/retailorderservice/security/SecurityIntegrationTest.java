package com.example.retailorderservice.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

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
    void publicHealthEndpointsDoNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void anonymousCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(productJson("HAMMER-001")))
                .andExpect(status().isUnauthorized());

        assertThat(productRepository.count()).isZero();
    }

    @Test
    void userCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/products")
                        .with(httpBasic("user", "user-password"))
                        .contentType(APPLICATION_JSON)
                        .content(productJson("HAMMER-001")))
                .andExpect(status().isForbidden());

        assertThat(productRepository.count()).isZero();
    }

    @Test
    void adminCanCreateProduct() throws Exception {
        mockMvc.perform(post("/products")
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(APPLICATION_JSON)
                        .content(productJson("HAMMER-001")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/products/\\d+")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.sku").value("HAMMER-001"));
    }

    @Test
    void userCanCreateOrder() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", 10));

        mockMvc.perform(post("/orders")
                        .with(httpBasic("user", "user-password"))
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(product.getId())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/orders/\\d+")))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void userCannotShipOrder() throws Exception {
        Long orderId = createOrderAsUser();

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .with(httpBasic("user", "user-password"))
                        .contentType(APPLICATION_JSON)
                        .content(shipJson()))
                .andExpect(status().isForbidden());

        assertThat(shipmentRepository.count()).isZero();
    }

    @Test
    void adminCanShipOrder() throws Exception {
        Long orderId = createOrderAsUser();

        mockMvc.perform(post("/orders/{id}/ship", orderId)
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(APPLICATION_JSON)
                        .content(shipJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/" + orderId + "/shipment"))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.carrier").value("UPS"));
    }

    private Long createOrderAsUser() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", 10));
        String location = mockMvc.perform(post("/orders")
                        .with(httpBasic("user", "user-password"))
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(product.getId())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        assertThat(location).isNotNull();
        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    private static Product product(String sku, int quantityAvailable) {
        return new Product(sku, "Steel Hammer", "16 oz steel hammer", new BigDecimal("19.99"), quantityAvailable, true);
    }

    private static String productJson(String sku) {
        return """
                {
                  "sku": "%s",
                  "name": "Steel Hammer",
                  "description": "16 oz steel hammer",
                  "price": 19.99,
                  "quantityAvailable": 10,
                  "active": true
                }
                """.formatted(sku);
    }

    private static String orderJson(Long productId) {
        return """
                {
                  "customerEmail": "customer@example.com",
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 1
                    }
                  ]
                }
                """.formatted(productId);
    }

    private static String shipJson() {
        return """
                {
                  "carrier": "UPS",
                  "trackingNumber": "1Z999999999"
                }
                """;
    }
}
