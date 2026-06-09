package com.example.retailorderservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.retailorderservice.dto.request.CreateOrderItemRequest;
import com.example.retailorderservice.dto.request.CreateOrderRequest;
import com.example.retailorderservice.dto.response.OrderResponse;
import com.example.retailorderservice.entity.Order;
import com.example.retailorderservice.entity.OrderStatus;
import com.example.retailorderservice.entity.Product;
import com.example.retailorderservice.exception.InsufficientInventoryException;
import com.example.retailorderservice.exception.ProductInactiveException;
import com.example.retailorderservice.exception.ProductNotFoundException;
import com.example.retailorderservice.repository.OrderRepository;
import com.example.retailorderservice.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, productRepository);
    }

    @Test
    void createsOrderWithOneItem() {
        Product product = product(1L, "HAMMER-001", "Steel Hammer", "19.99", 10, true);
        CreateOrderRequest request = orderRequest("customer@example.com", item(1L, 2));

        when(productRepository.findAllById(anyIterable())).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.orderNumber()).startsWith("ORD-");
        assertThat(response.customerEmail()).isEqualTo("customer@example.com");
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.subtotal()).isEqualByComparingTo("39.98");
        assertThat(response.tax()).isEqualByComparingTo("3.30");
        assertThat(response.total()).isEqualByComparingTo("43.28");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).skuSnapshot()).isEqualTo("HAMMER-001");
        assertThat(response.items().get(0).productNameSnapshot()).isEqualTo("Steel Hammer");
        assertThat(response.items().get(0).unitPriceSnapshot()).isEqualByComparingTo("19.99");
        assertThat(response.items().get(0).lineTotal()).isEqualByComparingTo("39.98");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createsOrderWithMultipleItemsAndCalculatesTotals() {
        Product hammer = product(1L, "HAMMER-001", "Steel Hammer", "19.99", 10, true);
        Product saw = product(2L, "SAW-001", "Hand Saw", "10.00", 5, true);
        CreateOrderRequest request = orderRequest("customer@example.com", item(1L, 2), item(2L, 1));

        when(productRepository.findAllById(anyIterable())).thenReturn(List.of(hammer, saw));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.subtotal()).isEqualByComparingTo("49.98");
        assertThat(response.tax()).isEqualByComparingTo("4.12");
        assertThat(response.total()).isEqualByComparingTo("54.10");
        assertThat(response.items()).hasSize(2);
    }

    @Test
    void rejectsMissingCustomerEmail() {
        CreateOrderRequest request = orderRequest(" ", item(1L, 1));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer email is required");
        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void rejectsEmptyItemList() {
        CreateOrderRequest request = new CreateOrderRequest("customer@example.com", List.of());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must have at least one item");
        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void rejectsNonexistentProduct() {
        CreateOrderRequest request = orderRequest("customer@example.com", item(99L, 1));

        when(productRepository.findAllById(anyIterable())).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void rejectsInactiveProduct() {
        Product product = product(1L, "HAMMER-001", "Steel Hammer", "19.99", 10, false);
        CreateOrderRequest request = orderRequest("customer@example.com", item(1L, 1));

        when(productRepository.findAllById(anyIterable())).thenReturn(List.of(product));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductInactiveException.class)
                .hasMessageContaining("1");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void rejectsInsufficientInventory() {
        Product product = product(1L, "HAMMER-001", "Steel Hammer", "19.99", 1, true);
        CreateOrderRequest request = orderRequest("customer@example.com", item(1L, 2));

        when(productRepository.findAllById(anyIterable())).thenReturn(List.of(product));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientInventoryException.class)
                .hasMessageContaining("requested 2, available 1");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deductsInventoryWhenOrderIsCreated() {
        Product product = product(1L, "HAMMER-001", "Steel Hammer", "19.99", 10, true);
        CreateOrderRequest request = orderRequest("customer@example.com", item(1L, 3));

        when(productRepository.findAllById(anyIterable())).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrder(request);

        assertThat(product.getQuantityAvailable()).isEqualTo(7);
    }

    private static CreateOrderRequest orderRequest(String customerEmail, CreateOrderItemRequest... items) {
        return new CreateOrderRequest(customerEmail, List.of(items));
    }

    private static CreateOrderItemRequest item(Long productId, int quantity) {
        return new CreateOrderItemRequest(productId, quantity);
    }

    private static Product product(Long id, String sku, String name, String price, int quantityAvailable, boolean active) {
        Product product = new Product(sku, name, name + " description", new BigDecimal(price), quantityAvailable, active);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
