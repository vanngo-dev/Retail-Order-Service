package com.example.retailorderservice.repository;

import com.example.retailorderservice.entity.Order;
import com.example.retailorderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByCustomerEmailIgnoreCase(String customerEmail, Pageable pageable);

    Page<Order> findByStatusAndCustomerEmailIgnoreCase(OrderStatus status, String customerEmail, Pageable pageable);
}
