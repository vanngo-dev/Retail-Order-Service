# Order Workflow Notes

## Goal

Build Phase 2 of the retail order service by adding order creation with order items, product lookup, inventory validation, price/name/SKU snapshots, subtotal/tax/total calculation, inventory deduction, automated tests, and documentation.

## What We Built

- `Order` JPA entity.
- `OrderItem` JPA entity.
- `OrderStatus` enum.
- Order and order item repositories.
- Create order request DTOs.
- Order and order item response DTOs.
- `OrderService` for business rules, totals, snapshots, and inventory deduction.
- `OrderController` with `/orders` endpoints.
- Unit tests and a Spring Boot API integration test suite.

## Why It Matters

Orders connect product catalog data to customer purchasing behavior. This phase turns the app from product CRUD into a business workflow: validate inventory, snapshot product information, calculate totals server-side, and update inventory in one transaction.

## Code Concepts

- One order has many order items.
- Order items snapshot product SKU, name, and unit price so historical orders do not change when products are edited later.
- The client sends product IDs and quantities, but the server calculates money values.
- Inventory validation happens before saving the order.
- Inventory deduction happens in the same transaction as order creation.
- Phase 2 uses a simplified fixed `8.25%` tax rate for portfolio purposes.

## Files Changed

- `src/main/java/com/example/retailorderservice/entity/Order.java`
- `src/main/java/com/example/retailorderservice/entity/OrderItem.java`
- `src/main/java/com/example/retailorderservice/entity/OrderStatus.java`
- `src/main/java/com/example/retailorderservice/entity/Product.java`
- `src/main/java/com/example/retailorderservice/repository/OrderRepository.java`
- `src/main/java/com/example/retailorderservice/repository/OrderItemRepository.java`
- `src/main/java/com/example/retailorderservice/dto/request/CreateOrderRequest.java`
- `src/main/java/com/example/retailorderservice/dto/request/CreateOrderItemRequest.java`
- `src/main/java/com/example/retailorderservice/dto/response/OrderResponse.java`
- `src/main/java/com/example/retailorderservice/dto/response/OrderItemResponse.java`
- `src/main/java/com/example/retailorderservice/mapper/OrderMapper.java`
- `src/main/java/com/example/retailorderservice/service/OrderService.java`
- `src/main/java/com/example/retailorderservice/controller/OrderController.java`
- `src/main/java/com/example/retailorderservice/exception/InsufficientInventoryException.java`
- `src/main/java/com/example/retailorderservice/exception/OrderNotFoundException.java`
- `src/main/java/com/example/retailorderservice/exception/ProductInactiveException.java`
- `src/test/java/com/example/retailorderservice/service/OrderServiceTest.java`
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

Create a product first:

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

Get the order:

```bash
curl http://localhost:8080/orders/1
```

List orders:

```bash
curl http://localhost:8080/orders
```

List created orders for one customer:

```bash
curl "http://localhost:8080/orders?status=CREATED&customerEmail=customer@example.com&page=0&size=20"
```

Show inventory deduction by checking the product:

```bash
curl http://localhost:8080/products/1
```

Show insufficient inventory handling:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productId": 1,
        "quantity": 999
      }
    ]
  }'
```

Show order validation:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "",
    "items": []
  }'
```

## How to Test

```bash
mvn test
```

The curl examples are for the video demo. The important order endpoint behavior is covered by `OrderApiIntegrationTest` so the phase can be verified automatically.

## Common Errors

- `400 Bad Request`: missing customer email, invalid email, empty items, missing product ID, or quantity less than one.
- `404 Not Found`: product ID or order ID does not exist.
- `409 Conflict`: product is inactive or requested quantity is greater than available inventory.
- Unexpected totals: Phase 2 uses a simplified fixed `8.25%` tax rate and rounds money values to two decimals.

## Interview Talking Points

- Explain why order items snapshot product price, name, and SKU.
- Explain why totals are calculated server-side instead of trusting client input.
- Explain why inventory must be checked before creating the order.
- Explain why order creation and inventory deduction belong in one transaction.
- Explain how service tests prove business rules and API integration tests prove endpoint behavior.

## Definition of Done

- `mvn test` passes.
- Automated tests cover the phase behavior.
- Manual curl demo commands are documented in this YouTube tutorial file.

## Commit Message

```bash
git commit -m "Phase 2: implement order creation and inventory deduction"
```
