package com.example.retailorderservice.repository;

import com.example.retailorderservice.entity.Shipment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    boolean existsByOrder_Id(Long orderId);

    Optional<Shipment> findByOrder_Id(Long orderId);
}
