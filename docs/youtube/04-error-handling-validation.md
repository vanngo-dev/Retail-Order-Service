# Error Handling and Validation Notes

## Goal

Build Phase 4 of the retail order service by adding centralized exception handling, consistent error responses, validation messages, proper HTTP status codes, automated tests for failure cases, and documentation.

## What We Built

- `ErrorResponse` DTO.
- `GlobalExceptionHandler`.
- `ResourceNotFoundException` base exception.
- Standard responses for validation errors, missing resources, duplicate SKU, insufficient inventory, invalid order state, duplicate shipment, malformed JSON, and unexpected server errors.
- Field-level validation details for invalid request payloads.
- API integration tests for important failure behavior.

## Why It Matters

Professional APIs should fail predictably. Clients need stable status codes and response fields so they can show useful messages, retry safely, and distinguish bad input from missing resources or business conflicts.

## Code Concepts

- `400 Bad Request` means the client sent an invalid request.
- `404 Not Found` means the requested resource does not exist.
- `409 Conflict` means the request is valid JSON, but it conflicts with business state.
- `500 Internal Server Error` hides unexpected server details from clients while logging the real exception.
- Centralized exception handling keeps controllers focused on successful workflows.

## Files Changed

- `src/main/java/com/example/retailorderservice/dto/response/ErrorResponse.java`
- `src/main/java/com/example/retailorderservice/exception/GlobalExceptionHandler.java`
- `src/main/java/com/example/retailorderservice/exception/ResourceNotFoundException.java`
- `src/main/java/com/example/retailorderservice/exception/ProductNotFoundException.java`
- `src/main/java/com/example/retailorderservice/exception/OrderNotFoundException.java`
- `src/main/java/com/example/retailorderservice/exception/DuplicateSkuException.java`
- `src/main/java/com/example/retailorderservice/exception/InsufficientInventoryException.java`
- `src/main/java/com/example/retailorderservice/exception/InvalidOrderStateException.java`
- `src/main/java/com/example/retailorderservice/exception/ProductInactiveException.java`
- `src/test/java/com/example/retailorderservice/controller/ApiErrorIntegrationTest.java`
- `src/test/java/com/example/retailorderservice/exception/GlobalExceptionHandlerTest.java`
- `README.md`
- `docs/api-examples.md`
- `docs/testing-guide.md`
- `docs/troubleshooting.md`
- `docs/architecture.md`
- `CHANGELOG.md`

## Standard Error Shape

```json
{
  "timestamp": "2026-06-04T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Name is required",
  "path": "/products"
}
```

Validation failures can also include field-level details:

```json
{
  "timestamp": "2026-06-04T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Name is required",
  "path": "/products",
  "validationErrors": {
    "name": "Name is required"
  }
}
```

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
    "quantityAvailable": 1,
    "active": true
  }'
```

Show duplicate SKU as `409 Conflict`:

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

Show missing product name as `400 Bad Request`:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SAW-001",
    "name": "",
    "description": "Missing name demo",
    "price": 14.99,
    "quantityAvailable": 10,
    "active": true
  }'
```

Show zero price as `400 Bad Request`:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SAW-001",
    "name": "Hand Saw",
    "description": "Zero price demo",
    "price": 0,
    "quantityAvailable": 10,
    "active": true
  }'
```

Show unknown order as `404 Not Found`:

```bash
curl http://localhost:8080/orders/999
```

Show insufficient inventory as `409 Conflict`:

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

Create another product for a successful order:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "DRILL-001",
    "name": "Cordless Drill",
    "description": "18V cordless drill",
    "price": 89.99,
    "quantityAvailable": 5,
    "active": true
  }'
```

Create an order using the returned drill product ID. Replace `2` with the actual product ID if your local database has existing data:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productId": 2,
        "quantity": 1
      }
    ]
  }'
```

Ship the order using the returned order ID. Replace `1` with the actual order ID from the previous response:

```bash
curl -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

Show duplicate shipment as `409 Conflict`. Use the same order ID again:

```bash
curl -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

Show malformed JSON as `400 Bad Request`:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"sku":'
```

## How to Test

```bash
mvn test
```

The curl examples are for the video demo. The important error behavior is covered by `ApiErrorIntegrationTest` and `GlobalExceptionHandlerTest` so the phase can be verified automatically.

## Common Errors

- `400 Bad Request`: invalid payload, validation failure, or malformed JSON.
- `404 Not Found`: requested product or order does not exist.
- `409 Conflict`: duplicate SKU, inactive product, insufficient inventory, already shipped order, cancelled order, or other invalid order state.
- `500 Internal Server Error`: unexpected server failure with details logged server-side.

## Interview Talking Points

- Explain why clients need predictable error response shapes.
- Explain why validation failures are different from business conflicts.
- Explain why duplicate SKU and duplicate shipment are `409 Conflict`.
- Explain why controllers should not each build their own error responses.
- Explain how automated tests protect API contracts.

## Definition of Done

- `mvn test` passes.
- Automated tests cover the phase behavior.
- Manual curl demo commands are documented in this YouTube tutorial file.

## Commit Message

```bash
git commit -m "Phase 4: add validation and centralized error handling"
```
