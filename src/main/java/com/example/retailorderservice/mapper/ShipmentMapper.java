package com.example.retailorderservice.mapper;

import com.example.retailorderservice.dto.response.ShipmentResponse;
import com.example.retailorderservice.entity.Shipment;

public final class ShipmentMapper {

    private ShipmentMapper() {
    }

    public static ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                shipment.getShippedAt(),
                shipment.getCreatedAt()
        );
    }
}
