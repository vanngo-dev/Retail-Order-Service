# Integration and Functional Testing Notes

## Goal

Build Phase 5 of the retail order service by adding integration and functional workflow tests that prove the API works end-to-end across controller, service, repository, validation, persistence, inventory deduction, order creation, shipment, and error handling.

## What We Built

- `OrderWorkflowIntegrationTest`.
- Full product-to-order-to-shipment workflow test.
- Failure workflow test for insufficient inventory.
- Failure workflow test for duplicate shipment.
- Failure workflow test for invalid product payloads.
- Documentation for running all tests and focused workflow tests.

## Why It Matters

Unit tests prove small business rules. Integration tests prove layers work together. Functional workflow tests prove the system supports the real user journey an employer or teammate cares about.

## Code Concepts

- Unit tests isolate service logic.
- Integration tests exercise controller, service, repository, validation, and database behavior together.
- Functional tests tell a story from the user's point of view.
- Repository assertions verify that API responses match persisted state.
- Failure workflow tests prove bad requests do not leave unwanted data behind.

## Files Changed

- `src/test/java/com/example/retailorderservice/workflow/OrderWorkflowIntegrationTest.java`
- `README.md`
- `docs/testing-guide.md`
- `docs/architecture.md`
- `docs/youtube/05-integration-functional-testing.md`
- `CHANGELOG.md`

## Required Functional Test

```text
Given a product exists with quantity 10
When a customer orders quantity 2
Then the order is created
And inventory becomes 8
When the order is shipped
Then order status becomes SHIPPED
And shipment data is returned
```

## How to Run

Run all tests:

```bash
mvn test
```

Run only the Phase 5 workflow tests:

```bash
mvn test -Dtest=OrderWorkflowIntegrationTest
```

## Manual Demo Flow

These curl commands are for the video/demo walkthrough. Automated tests in `src/test/java/...` are the verification method for this phase.

Start the app:

```bash
mvn spring-boot:run
```

Create a product with quantity `10`:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "HAMMER-001",
    "name": "Steel Hammer",
    "description": "16 oz steel hammer",
    "price": 19.99,
    "quantityAvailable": 10,
    "active": true
  }'
```

Create an order for quantity `2`. Replace `1` with the product ID returned by the previous response if needed:

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

Verify inventory is now `8`:

```bash
curl http://localhost:8080/products/1
```

Ship the order. Replace `1` with the order ID returned by the order response if needed:

```bash
curl -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

Verify the order is `SHIPPED`:

```bash
curl http://localhost:8080/orders/1
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

Show duplicate shipment handling:

```bash
curl -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "FedEx",
    "trackingNumber": "999999999999"
  }'
```

## How to Test

```bash
mvn test
```

The curl examples are for the video demo. The important workflow behavior is covered by `OrderWorkflowIntegrationTest` so the phase can be verified automatically.

## Common Errors

- `400 Bad Request`: invalid product, order, or shipment payload.
- `409 Conflict`: insufficient inventory or duplicate shipment.
- Hard-coded demo IDs may differ if the local in-memory database already contains data.

## Interview Talking Points

- Explain the difference between unit, integration, and functional tests.
- Explain why workflow tests catch bugs that isolated unit tests can miss.
- Explain why persistence assertions matter after API calls.
- Explain why failed workflow tests should verify that state did not change.
- Explain why running `mvn test` before every commit is part of professional delivery.

## Definition of Done

- `mvn test` passes.
- Automated tests cover the phase behavior.
- Manual curl demo commands are documented in this YouTube tutorial file.

## Commit Message

```bash
git commit -m "Phase 5: add integration and functional workflow tests"
```
