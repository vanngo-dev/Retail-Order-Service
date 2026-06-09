# Shipment Workflow Notes

## Goal

Build Phase 3 of the retail order service by adding shipment persistence, order status transition from `CREATED` to `SHIPPED`, invalid state handling, automated tests, and documentation.

## What We Built

- `Shipment` JPA entity.
- `ShipmentRepository`.
- `ShipOrderRequest` DTO.
- `ShipmentResponse` DTO.
- `ShipmentMapper`.
- `InvalidOrderStateException`.
- `OrderService.shipOrder`.
- `POST /orders/{id}/ship` endpoint.
- Unit tests and a Spring Boot API integration test suite.

## Why It Matters

Shipping is a business operation, not a simple field update. A real system needs to record shipment details, prevent duplicate shipments, and make order status transitions predictable.

## Code Concepts

- One order can have one shipment in this MVP.
- Shipping creates a shipment row and updates the order status in the same transaction.
- Only `CREATED` orders can be shipped.
- Already shipped orders are rejected.
- Cancelled orders are rejected.
- Bean Validation rejects missing carrier and tracking number.

## Files Changed

- `src/main/java/com/example/retailorderservice/entity/Shipment.java`
- `src/main/java/com/example/retailorderservice/entity/Order.java`
- `src/main/java/com/example/retailorderservice/repository/ShipmentRepository.java`
- `src/main/java/com/example/retailorderservice/dto/request/ShipOrderRequest.java`
- `src/main/java/com/example/retailorderservice/dto/response/ShipmentResponse.java`
- `src/main/java/com/example/retailorderservice/mapper/ShipmentMapper.java`
- `src/main/java/com/example/retailorderservice/exception/InvalidOrderStateException.java`
- `src/main/java/com/example/retailorderservice/service/OrderService.java`
- `src/main/java/com/example/retailorderservice/controller/OrderController.java`
- `src/test/java/com/example/retailorderservice/service/OrderServiceTest.java`
- `src/test/java/com/example/retailorderservice/controller/ShipmentApiIntegrationTest.java`
- `src/test/java/com/example/retailorderservice/controller/OrderApiIntegrationTest.java`
- `src/test/java/com/example/retailorderservice/controller/ProductApiIntegrationTest.java`
- `README.md`
- `docs/api-examples.md`
- `docs/testing-guide.md`
- `docs/troubleshooting.md`
- `docs/architecture.md`
- `CHANGELOG.md`

## How to Run

```bash
mvn spring-boot:run
```

## Manual Demo Flow

These curl commands are for the video/demo walkthrough. Automated tests in `src/test/java/...` are the verification method for this phase.

Check health:

```bash
curl http://localhost:8080/actuator/health
```

Create a product:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "HAMMER-001",
    "name": "Steel Hammer",
    "description": "16 oz steel hammer",
    "price": 19.99,
    "quantityAvailable": 100,
    "active": true
  }'
```

Create an order:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

Ship the order:

```bash
curl -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

Confirm the order status changed to `SHIPPED`:

```bash
curl http://localhost:8080/orders/1
```

Show duplicate shipment handling:

```bash
curl -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

Show missing tracking validation:

```bash
curl -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": ""
  }'
```

## How to Test

```bash
mvn test
```

The curl examples are for the video demo. The important shipment endpoint behavior is covered by `ShipmentApiIntegrationTest` so the phase can be verified automatically.

## Common Errors

- `400 Bad Request`: missing carrier or tracking number.
- `404 Not Found`: order ID does not exist.
- `409 Conflict`: order is already shipped, cancelled, or not in `CREATED` status.

## Interview Talking Points

- Explain why shipping is modeled as a business operation.
- Explain why one order should not be shipped twice.
- Explain why order status transitions need rules.
- Explain why shipment persistence and status update belong in one transaction.
- Explain how automated tests protect state transition behavior.

## Definition of Done

- `mvn test` passes.
- Automated tests cover the phase behavior.
- Manual curl demo commands are documented in this YouTube tutorial file.

## Commit Message

```bash
git commit -m "Phase 3: implement shipment workflow and order status transition"
```
