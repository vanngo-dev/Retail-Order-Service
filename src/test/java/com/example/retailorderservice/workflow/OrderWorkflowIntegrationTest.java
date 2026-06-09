package com.example.retailorderservice.workflow;

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
import com.example.retailorderservice.entity.Shipment;
import com.example.retailorderservice.repository.OrderItemRepository;
import com.example.retailorderservice.repository.OrderRepository;
import com.example.retailorderservice.repository.ProductRepository;
import com.example.retailorderservice.repository.ShipmentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class OrderWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void completeOrderFulfillmentWorkflowCreatesOrderDeductsInventoryAndShipsOrder() throws Exception {
        Long productId = createProduct("HAMMER-001", "Steel Hammer", "19.99", 10);

        Long orderId = createOrder(productId, 2);

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.quantityAvailable").value(8));

        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getQuantityAvailable()).isEqualTo(8);

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getSubtotal()).isEqualByComparingTo("39.98");
        assertThat(order.getTax()).isEqualByComparingTo("3.30");
        assertThat(order.getTotal()).isEqualByComparingTo("43.28");
        assertThat(orderItemRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.subtotal").value(39.98))
                .andExpect(jsonPath("$.tax").value(3.30))
                .andExpect(jsonPath("$.total").value(43.28))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(productId))
                .andExpect(jsonPath("$.items[0].skuSnapshot").value("HAMMER-001"))
                .andExpect(jsonPath("$.items[0].productNameSnapshot").value("Steel Hammer"))
                .andExpect(jsonPath("$.items[0].unitPriceSnapshot").value(19.99))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].lineTotal").value(39.98));

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

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.items[0].productId").value(productId))
                .andExpect(jsonPath("$.items[0].skuSnapshot").value("HAMMER-001"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        Shipment shipment = shipmentRepository.findByOrder_Id(orderId).orElseThrow();
        assertThat(shipment.getCarrier()).isEqualTo("UPS");
        assertThat(shipment.getTrackingNumber()).isEqualTo("1Z999999999");
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void orderFailureWorkflowDoesNotDeductInventoryOrCreateOrder() throws Exception {
        Long productId = createProduct("SAW-001", "Hand Saw", "14.99", 1);

        mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(productId, 2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Insufficient inventory for product id %d: requested 2, available 1".formatted(productId)))
                .andExpect(jsonPath("$.path").value("/orders"));

        assertThat(productRepository.findById(productId).orElseThrow().getQuantityAvailable()).isEqualTo(1);
        assertThat(orderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
        assertThat(shipmentRepository.count()).isZero();
    }

    @Test
    void duplicateShipmentWorkflowKeepsSingleShipmentAndShippedStatus() throws Exception {
        Long productId = createProduct("DRILL-001", "Cordless Drill", "89.99", 5);
        Long orderId = createOrder(productId, 1);

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
        Shipment shipment = shipmentRepository.findByOrder_Id(orderId).orElseThrow();
        assertThat(shipment.getCarrier()).isEqualTo("UPS");
        assertThat(shipment.getTrackingNumber()).isEqualTo("1Z999999999");
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void productValidationWorkflowDoesNotPersistInvalidProduct() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(productJson("INVALID-001", "", "9.99", 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Name is required"))
                .andExpect(jsonPath("$.validationErrors.name").value("Name is required"));

        assertThat(productRepository.count()).isZero();
    }

    private Long createProduct(String sku, String name, String price, int quantityAvailable) throws Exception {
        String responseBody = mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(productJson(sku, name, price, quantityAvailable)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/products/\\d+")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.sku").value(sku))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.quantityAvailable").value(quantityAvailable))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return readId(responseBody);
    }

    private Long createOrder(Long productId, int quantity) throws Exception {
        String responseBody = mockMvc.perform(post("/orders")
                        .contentType(APPLICATION_JSON)
                        .content(orderJson(productId, quantity)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/orders/\\d+")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.items[0].productId").value(productId))
                .andExpect(jsonPath("$.items[0].quantity").value(quantity))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return readId(responseBody);
    }

    private Long readId(String responseBody) throws Exception {
        JsonNode json = objectMapper.readTree(responseBody);
        return json.get("id").asLong();
    }

    private static String productJson(String sku, String name, String price, int quantityAvailable) {
        return """
                {
                  "sku": "%s",
                  "name": "%s",
                  "description": "%s description",
                  "price": %s,
                  "quantityAvailable": %d,
                  "active": true
                }
                """.formatted(sku, name, name, price, quantityAvailable);
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
