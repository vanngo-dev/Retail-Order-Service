# Destructive Testing and Resilience Notes

## Goal

Build Phase 6 of the retail order service by adding destructive and resilience-focused automated tests that intentionally send invalid payloads, malformed JSON, duplicate operations, invalid IDs, insufficient inventory cases, inactive product cases, and invalid order state transitions.

## What We Built

- `DestructiveApiIntegrationTest`.
- Malformed JSON tests.
- Invalid product payload tests.
- Invalid order payload tests.
- Invalid shipment payload tests.
- Duplicate SKU and duplicate shipment tests.
- Nonexistent product and order ID tests.
- Insufficient inventory and inactive product tests.
- Cancelled order shipment test.
- State-safety assertions after failed requests.

## Why It Matters

Destructive testing means intentionally testing what happens when users or systems do the wrong thing. Production APIs receive bad input constantly, so reliable systems must fail safely, return predictable errors, and avoid corrupting persisted state.

## Code Concepts

- Bad input should produce `400 Bad Request`.
- Missing resources should produce `404 Not Found`.
- Business conflicts should produce `409 Conflict`.
- Failed create requests should not leave partial rows behind.
- Duplicate operations should not create duplicate state.
- Invalid state transitions should preserve the existing state.

## Files Changed

- `src/test/java/com/example/retailorderservice/destructive/DestructiveApiIntegrationTest.java`
- `README.md`
- `docs/testing-guide.md`
- `docs/architecture.md`
- `docs/troubleshooting.md`
- `docs/youtube/06-destructive-testing.md`
- `CHANGELOG.md`

## How to Run

Run all tests:

```bash
mvn test
```

Run only the Phase 6 destructive tests:

```bash
mvn test -Dtest=DestructiveApiIntegrationTest
```

## Manual Demo Flow

These curl commands are for the video/demo walkthrough. Automated tests in `src/test/java/...` are the verification method for this phase.

Start the app:

```bash
mvn spring-boot:run
```

Show malformed JSON:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"sku":'
```

Show missing product fields:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "description": "Invalid product demo",
    "price": 0,
    "quantityAvailable": -1,
    "active": true
  }'
```

Create a valid product:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "HAMMER-001",
    "name": "Steel Hammer",
    "description": "16 oz steel hammer",
    "price": 19.99,
    "quantityAvailable": 1,
    "active": true
  }'
```

Show duplicate SKU:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "hammer-001",
    "name": "Duplicate Hammer",
    "description": "Duplicate SKU demo",
    "price": 21.99,
    "quantityAvailable": 5,
    "active": true
  }'
```

Show bad order payload:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "not-an-email",
    "items": []
  }'
```

Show nonexistent product ID:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productId": 999,
        "quantity": 1
      }
    ]
  }'
```

Show insufficient inventory. Replace `1` with the product ID from the valid product response if needed:

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

Create a valid order. Replace `1` with the product ID from the valid product response if needed:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productId": 1,
        "quantity": 1
      }
    ]
  }'
```

Show missing shipment fields. Replace `1` with the order ID from the valid order response if needed:

```bash
curl -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "",
    "trackingNumber": ""
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

Show duplicate shipment:

```bash
curl -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "FedEx",
    "trackingNumber": "999999999999"
  }'
```

Show invalid path ID:

```bash
curl http://localhost:8080/products/not-a-number
```

## How to Test

```bash
mvn test
```

The curl examples are for the video demo. The important destructive behavior is covered by `DestructiveApiIntegrationTest` so the phase can be verified automatically.

## Common Errors

- `400 Bad Request`: malformed JSON, missing fields, invalid values, invalid path IDs.
- `404 Not Found`: nonexistent product or order IDs.
- `409 Conflict`: duplicate SKU, insufficient inventory, inactive products, duplicate shipment, cancelled order shipment.
- Demo IDs may differ if your local in-memory database already contains data.

## Interview Talking Points

- Explain what destructive testing means.
- Explain why bad input is normal in production.
- Explain why reliable systems must fail safely.
- Explain why tests should verify state after failed requests.
- Explain how destructive testing maps to Software Engineer II responsibilities.

## Definition of Done

- `mvn test` passes.
- Automated tests cover the phase behavior.
- Manual curl demo commands are documented in this YouTube tutorial file.

## Commit Message

```bash
git commit -m "Phase 6: add destructive testing and resilience scenarios"
```
