# Logging, Monitoring, Health Checks, and Diagnostics Notes

## Goal

Build Phase 9 of the retail order service by adding production-oriented logging, Actuator diagnostics, `/actuator/info`, service metadata, and basic observability documentation.

## What We Built

- Structured key-value logs in the service layer.
- `INFO` logs for successful product, order, inventory, and shipment events.
- `WARN` logs for duplicate SKU, inactive product, insufficient inventory, and invalid shipment state attempts.
- Actuator exposure for `/actuator/health` and `/actuator/info`.
- Liveness and readiness health probes.
- Service metadata under `/actuator/info`.
- Automated tests for health, info, and representative log output.

## Why It Matters

Production support is about understanding what the system is doing after it leaves a developer laptop. Health endpoints show whether the service is alive and ready, info endpoints identify the running service, and useful logs make root-cause analysis possible when a workflow fails.

## Code Concepts

- `INFO` logs record successful business milestones.
- `WARN` logs record expected but important business failures.
- `ERROR` logs are reserved for unexpected failures.
- Logs use key-value fields such as `orderId`, `productId`, `quantityDeducted`, and `total`.
- Logs avoid unnecessary sensitive data such as customer email and shipment tracking numbers.
- Actuator endpoint exposure is configured in `application.yml`.

## Files Changed

- `src/main/java/com/example/retailorderservice/service/ProductService.java`
- `src/main/java/com/example/retailorderservice/service/OrderService.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-docker.yml`
- `src/test/java/com/example/retailorderservice/controller/HealthControllerTest.java`
- `src/test/java/com/example/retailorderservice/service/ProductServiceTest.java`
- `src/test/java/com/example/retailorderservice/service/OrderServiceTest.java`
- `README.md`
- `docs/testing-guide.md`
- `docs/architecture.md`
- `docs/troubleshooting.md`
- `docs/youtube/09-logging-monitoring-diagnostics.md`
- `CHANGELOG.md`

## How to Run

Run the app locally:

```bash
mvn spring-boot:run
```

## Manual Diagnostic Demo Flow

These curl commands are for the video/demo walkthrough. Automated tests in `src/test/java/...` are the verification method for this phase.

Check Actuator health:

```bash
curl http://localhost:8080/actuator/health
```

Check liveness:

```bash
curl http://localhost:8080/actuator/health/liveness
```

Check readiness:

```bash
curl http://localhost:8080/actuator/health/readiness
```

Check service metadata:

```bash
curl http://localhost:8080/actuator/info
```

Create a product and watch for the `Product created` log:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "LOG-HAMMER-001",
    "name": "Logging Demo Hammer",
    "description": "Product used to demonstrate service logs",
    "price": 19.99,
    "quantityAvailable": 2,
    "active": true
  }'
```

Create an order and watch for `Inventory deducted` and `Order created` logs. Replace `PRODUCT_ID` with the ID returned by the product create response:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "demo@example.com",
    "items": [
      {
        "productId": PRODUCT_ID,
        "quantity": 1
      }
    ]
  }'
```

Ship the order and watch for the `Shipment created` log. Replace `ORDER_ID` with the ID returned by the order create response:

```bash
curl -X POST http://localhost:8080/orders/ORDER_ID/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

Trigger an insufficient inventory warning. Replace `PRODUCT_ID` with the product ID:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "demo@example.com",
    "items": [
      {
        "productId": PRODUCT_ID,
        "quantity": 99
      }
    ]
  }'
```

The API should return `409 Conflict`, and the application logs should include `Insufficient inventory`.

## How to Test

Run the automated test suite:

```bash
mvn test
```

Run only the diagnostic endpoint tests:

```bash
mvn test -Dtest=HealthControllerTest
```

## Common Errors

- `/actuator/info` returns `{}`: confirm `management.info.env.enabled=true` and `info.app` metadata are configured.
- `/actuator/info` returns `404`: confirm `info` is included in Actuator endpoint exposure.
- Liveness or readiness endpoint returns `404`: confirm health probes are enabled.
- Logs do not appear: confirm `com.example.retailorderservice` is configured at `INFO` level.
- Logs contain too much data: remove customer email, full request bodies, passwords, and tracking numbers from routine logs.

## Interview Talking Points

- Explain how logs support root-cause analysis.
- Explain the difference between `DEBUG`, `INFO`, `WARN`, and `ERROR`.
- Explain why health endpoints matter in cloud and microservice systems.
- Explain why `/actuator/info` helps identify a running service.
- Explain why operational diagnostics should avoid leaking sensitive data.

## Definition of Done

- `mvn test` passes.
- Health and info diagnostics are exposed.
- Service metadata is available through `/actuator/info`.
- Service logs cover important successful and failed business events.
- Manual diagnostic curl commands are documented in this YouTube tutorial file.

## Commit Message

```bash
git commit -m "Phase 9: add logging and actuator diagnostics"
```
