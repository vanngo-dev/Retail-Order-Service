package com.example.retailorderservice.service;

import com.example.retailorderservice.dto.request.CreateOrderItemRequest;
import com.example.retailorderservice.dto.request.CreateOrderRequest;
import com.example.retailorderservice.dto.request.ShipOrderRequest;
import com.example.retailorderservice.dto.response.OrderResponse;
import com.example.retailorderservice.dto.response.ShipmentResponse;
import com.example.retailorderservice.entity.Order;
import com.example.retailorderservice.entity.OrderItem;
import com.example.retailorderservice.entity.OrderStatus;
import com.example.retailorderservice.entity.Product;
import com.example.retailorderservice.entity.Shipment;
import com.example.retailorderservice.exception.InsufficientInventoryException;
import com.example.retailorderservice.exception.InvalidOrderStateException;
import com.example.retailorderservice.exception.OrderNotFoundException;
import com.example.retailorderservice.exception.ProductInactiveException;
import com.example.retailorderservice.exception.ProductNotFoundException;
import com.example.retailorderservice.mapper.OrderMapper;
import com.example.retailorderservice.mapper.ShipmentMapper;
import com.example.retailorderservice.repository.OrderRepository;
import com.example.retailorderservice.repository.ProductRepository;
import com.example.retailorderservice.repository.ShipmentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private static final BigDecimal TAX_RATE = new BigDecimal("0.0825");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ShipmentRepository shipmentRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            ShipmentRepository shipmentRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(OrderStatus status, String customerEmail, Pageable pageable) {
        String normalizedEmail = normalize(customerEmail);

        if (status != null && StringUtils.hasText(normalizedEmail)) {
            return orderRepository.findByStatusAndCustomerEmailIgnoreCase(status, normalizedEmail, pageable)
                    .map(OrderMapper::toResponse);
        }

        if (status != null) {
            return orderRepository.findByStatus(status, pageable).map(OrderMapper::toResponse);
        }

        if (StringUtils.hasText(normalizedEmail)) {
            return orderRepository.findByCustomerEmailIgnoreCase(normalizedEmail, pageable)
                    .map(OrderMapper::toResponse);
        }

        return orderRepository.findAll(pageable).map(OrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return OrderMapper.toResponse(findOrder(id));
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        String customerEmail = normalizeRequired(request.customerEmail(), "Customer email is required");
        List<CreateOrderItemRequest> requestedItems = validateItems(request.items());
        Map<Long, Integer> requestedQuantitiesByProductId = requestedQuantitiesByProductId(requestedItems);
        Map<Long, Product> productsById = findProducts(requestedQuantitiesByProductId);

        requestedQuantitiesByProductId.forEach((productId, quantity) -> validateProductCanBeOrdered(productsById.get(productId), quantity));

        BigDecimal subtotal = calculateSubtotal(requestedItems, productsById);
        BigDecimal tax = calculateTax(subtotal);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        Order order = new Order(generateOrderNumber(), customerEmail, subtotal, tax, total);
        requestedItems.forEach(item -> addOrderItem(order, productsById.get(item.productId()), item.quantity()));
        requestedQuantitiesByProductId.forEach((productId, quantity) -> deductInventory(productsById.get(productId), quantity));

        Order savedOrder = orderRepository.save(order);
        LOGGER.info(
                "Order created: orderId={}, orderNumber={}, itemCount={}, subtotal={}, tax={}, total={}",
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getItems().size(),
                savedOrder.getSubtotal(),
                savedOrder.getTax(),
                savedOrder.getTotal()
        );
        return OrderMapper.toResponse(savedOrder);
    }

    public ShipmentResponse shipOrder(Long orderId, ShipOrderRequest request) {
        String carrier = normalizeRequired(request.carrier(), "Carrier is required");
        String trackingNumber = normalizeRequired(request.trackingNumber(), "Tracking number is required");
        Order order = findOrder(orderId);

        validateOrderCanBeShipped(order);

        Shipment shipment = new Shipment(order, carrier, trackingNumber, Instant.now());
        order.markShipped();

        Shipment savedShipment = shipmentRepository.save(shipment);
        LOGGER.info(
                "Shipment created: shipmentId={}, orderId={}, carrier={}",
                savedShipment.getId(),
                order.getId(),
                savedShipment.getCarrier()
        );
        return ShipmentMapper.toResponse(savedShipment);
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private static List<CreateOrderItemRequest> validateItems(List<CreateOrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        items.forEach(item -> {
            if (item == null) {
                throw new IllegalArgumentException("Order item is required");
            }
            if (item.productId() == null) {
                throw new IllegalArgumentException("Product ID is required");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
        });

        return items;
    }

    private static Map<Long, Integer> requestedQuantitiesByProductId(List<CreateOrderItemRequest> items) {
        Map<Long, Integer> quantitiesByProductId = new LinkedHashMap<>();
        items.forEach(item -> quantitiesByProductId.merge(item.productId(), item.quantity(), Integer::sum));
        return quantitiesByProductId;
    }

    private Map<Long, Product> findProducts(Map<Long, Integer> requestedQuantitiesByProductId) {
        Map<Long, Product> productsById = productRepository.findAllById(requestedQuantitiesByProductId.keySet())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        requestedQuantitiesByProductId.keySet().forEach(productId -> {
            if (!productsById.containsKey(productId)) {
                throw new ProductNotFoundException(productId);
            }
        });

        return productsById;
    }

    private static void validateProductCanBeOrdered(Product product, int requestedQuantity) {
        if (!product.isActive()) {
            LOGGER.warn("Inactive product order rejected: productId={}", product.getId());
            throw new ProductInactiveException(product.getId());
        }
        if (product.getQuantityAvailable() < requestedQuantity) {
            LOGGER.warn(
                    "Insufficient inventory: productId={}, requestedQuantity={}, availableQuantity={}",
                    product.getId(),
                    requestedQuantity,
                    product.getQuantityAvailable()
            );
            throw new InsufficientInventoryException(product.getId(), requestedQuantity, product.getQuantityAvailable());
        }
    }

    private void validateOrderCanBeShipped(Order order) {
        if (order.getStatus() == OrderStatus.SHIPPED || shipmentRepository.existsByOrder_Id(order.getId())) {
            LOGGER.warn("Invalid order state attempted: orderId={}, status={}, reason=already-shipped", order.getId(), order.getStatus());
            throw new InvalidOrderStateException("Order has already been shipped: " + order.getId());
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            LOGGER.warn("Invalid order state attempted: orderId={}, status={}, reason=cancelled", order.getId(), order.getStatus());
            throw new InvalidOrderStateException("Cancelled order cannot be shipped: " + order.getId());
        }
        if (order.getStatus() != OrderStatus.CREATED) {
            LOGGER.warn("Invalid order state attempted: orderId={}, status={}, reason=not-created", order.getId(), order.getStatus());
            throw new InvalidOrderStateException("Only CREATED orders can be shipped: " + order.getId());
        }
    }

    private static void deductInventory(Product product, int quantity) {
        product.deductInventory(quantity);
        LOGGER.info(
                "Inventory deducted: productId={}, quantityDeducted={}, quantityAvailable={}",
                product.getId(),
                quantity,
                product.getQuantityAvailable()
        );
    }

    private static BigDecimal calculateSubtotal(List<CreateOrderItemRequest> items, Map<Long, Product> productsById) {
        return items.stream()
                .map(item -> calculateLineTotal(productsById.get(item.productId()), item.quantity()))
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private static void addOrderItem(Order order, Product product, int quantity) {
        BigDecimal unitPrice = product.getPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal lineTotal = calculateLineTotal(product, quantity);
        order.addItem(new OrderItem(
                product.getId(),
                product.getSku(),
                product.getName(),
                unitPrice,
                quantity,
                lineTotal
        ));
    }

    private static BigDecimal calculateLineTotal(Product product, int quantity) {
        return product.getPrice()
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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
