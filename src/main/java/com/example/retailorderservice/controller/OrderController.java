package com.example.retailorderservice.controller;

import com.example.retailorderservice.dto.request.CreateOrderRequest;
import com.example.retailorderservice.dto.request.ShipOrderRequest;
import com.example.retailorderservice.dto.response.OrderResponse;
import com.example.retailorderservice.dto.response.ShipmentResponse;
import com.example.retailorderservice.entity.OrderStatus;
import com.example.retailorderservice.service.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Page<OrderResponse> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerEmail,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return orderService.listOrders(status, customerEmail, pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity
                .created(URI.create("/orders/" + response.id()))
                .body(response);
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<ShipmentResponse> shipOrder(
            @PathVariable Long id,
            @Valid @RequestBody ShipOrderRequest request
    ) {
        ShipmentResponse response = orderService.shipOrder(id, request);
        return ResponseEntity
                .created(URI.create("/orders/" + id + "/shipment"))
                .body(response);
    }
}
