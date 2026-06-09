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

import com.example.retailorderservice.entity.Product;
import com.example.retailorderservice.repository.OrderItemRepository;
import com.example.retailorderservice.repository.OrderRepository;
import com.example.retailorderservice.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void cleanDatabase() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void createsOrderWithInventoryDeductionAndSnapshots() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, true));

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson("customer@example.com", orderItemJson(product.getId(), 2))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/orders/\\d+")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.orderNumber", matchesPattern("ORD-[A-Z0-9]{8}")))
                .andExpect(jsonPath("$.customerEmail").value("customer@example.com"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.subtotal").value(39.98))
                .andExpect(jsonPath("$.tax").value(3.30))
                .andExpect(jsonPath("$.total").value(43.28))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()))
                .andExpect(jsonPath("$.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.items[0].skuSnapshot").value("HAMMER-001"))
                .andExpect(jsonPath("$.items[0].productNameSnapshot").value("Steel Hammer"))
                .andExpect(jsonPath("$.items[0].unitPriceSnapshot").value(19.99))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].lineTotal").value(39.98));

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantityAvailable()).isEqualTo(8);
    }

    @Test
    void createsOrderWithMultipleItemsAndCalculatedTotals() throws Exception {
        Product hammer = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, true));
        Product saw = productRepository.save(product("SAW-001", "Hand Saw", "10.00", 5, true));

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson("customer@example.com", orderItemJson(hammer.getId(), 2), orderItemJson(saw.getId(), 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(49.98))
                .andExpect(jsonPath("$.tax").value(4.12))
                .andExpect(jsonPath("$.total").value(54.10))
                .andExpect(jsonPath("$.items.length()").value(2));

        assertThat(productRepository.findById(hammer.getId()).orElseThrow().getQuantityAvailable()).isEqualTo(8);
        assertThat(productRepository.findById(saw.getId()).orElseThrow().getQuantityAvailable()).isEqualTo(4);
    }

    @Test
    void getsOrderById() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, true));
        Long orderId = createOrder(product.getId());

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.items[0].skuSnapshot").value("HAMMER-001"));
    }

    @Test
    void listsOrdersWithFiltersAndPagination() throws Exception {
        Product hammer = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, true));
        Product saw = productRepository.save(product("SAW-001", "Hand Saw", "10.00", 5, true));
        createOrder("customer@example.com", hammer.getId(), 1);
        createOrder("other@example.com", saw.getId(), 1);

        mockMvc.perform(get("/orders")
                        .param("status", "CREATED")
                        .param("customerEmail", "CUSTOMER@example.com")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content[0].customerEmail").value("customer@example.com"));
    }

    @Test
    void rejectsMissingCustomerEmail() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, true));

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson("", orderItemJson(product.getId(), 1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEmptyItemList() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "customerEmail": "customer@example.com",
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsQuantityLessThanOne() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, true));

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson("customer@example.com", orderItemJson(product.getId(), 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsNonexistentProduct() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson("customer@example.com", orderItemJson(999L, 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInactiveProduct() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 10, false));

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson("customer@example.com", orderItemJson(product.getId(), 1))))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInsufficientInventory() throws Exception {
        Product product = productRepository.save(product("HAMMER-001", "Steel Hammer", "19.99", 1, true));

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson("customer@example.com", orderItemJson(product.getId(), 2))))
                .andExpect(status().isConflict());
    }

    @Test
    void returnsNotFoundForUnknownOrder() throws Exception {
        mockMvc.perform(get("/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    private Long createOrder(Long productId) throws Exception {
        return createOrder("customer@example.com", productId, 1);
    }

    private Long createOrder(String customerEmail, Long productId, int quantity) throws Exception {
        String location = mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(customerEmail, orderItemJson(productId, quantity))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        assertThat(location).isNotNull();
        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    private static Product product(String sku, String name, String price, int quantityAvailable, boolean active) {
        return new Product(sku, name, name + " description", new BigDecimal(price), quantityAvailable, active);
    }

    private static String orderJson(String customerEmail, String... itemJson) {
        return """
                {
                  "customerEmail": "%s",
                  "items": [
                    %s
                  ]
                }
                """.formatted(customerEmail, String.join(",\n    ", itemJson));
    }

    private static String orderItemJson(Long productId, int quantity) {
        return """
                {
                  "productId": %d,
                  "quantity": %d
                }
                """.formatted(productId, quantity);
    }
}
