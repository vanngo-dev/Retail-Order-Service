# Phase 12 - Final Documentation, Demo Script, and Portfolio Polish

This final phase prepares the project for GitHub, resume use, interview walkthroughs, LinkedIn, and a YouTube/demo presentation.

## Goal

Show the completed Retail Order Service as a production-style Spring Boot backend project with:

- Product catalog management.
- Order creation with inventory validation and deduction.
- Shipment workflow and order status transition.
- Centralized validation and error handling.
- Automated tests.
- Docker and PostgreSQL runtime support.
- GitHub Actions CI.
- Logging, health checks, diagnostics, security, and performance testing exposure.

## Final Project Summary

This project demonstrates my ability to build and support production-style backend services using Java, Spring Boot, SQL, REST APIs, validation, testing, Docker, CI/CD, logging, monitoring, and security fundamentals. The domain models a realistic retail workflow with products, orders, inventory, and shipments, making it relevant to business software and retail technology environments.

## Demo Setup

These commands are shown in Bash/Git Bash style. If using PowerShell, use `curl.exe` and adjust line continuation as needed.

Use a fresh local H2 run for the cleanest demo so product ID `1` and order ID `1` are easy to follow.

Demo credentials:

| Username | Password | Role |
|---|---|---|
| `user` | `user-password` | `USER` |
| `admin` | `admin-password` | `USER`, `ADMIN` |

## Demo Flow

### 1. Start The Application

```bash
mvn spring-boot:run
```

Narration:

- The app starts locally with H2 by default.
- Docker mode uses PostgreSQL, but local development remains lightweight.

### 2. Show Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

Expected result:

```json
{
  "status": "UP"
}
```

Optional service metadata:

```bash
curl http://localhost:8080/actuator/info
```

### 3. Create A Product

```bash
curl -u admin:admin-password -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "DEMO-HAMMER-012",
    "name": "Demo Steel Hammer",
    "description": "Portfolio demo product",
    "price": 19.99,
    "quantityAvailable": 10,
    "active": true
  }'
```

Narration:

- Product writes require the `ADMIN` role.
- SKU values are unique.
- Validation protects required fields, price, and inventory quantity.

### 4. List Products

```bash
curl "http://localhost:8080/products?active=true&page=0&size=20"
```

Narration:

- Product reads are public for the demo.
- The list endpoint supports filtering and pagination.

### 5. Create An Order

```bash
curl -u user:user-password -X POST http://localhost:8080/orders \
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

Narration:

- Order creation requires `USER` or `ADMIN`.
- The service validates product existence, product active status, and available inventory.
- Product SKU, name, and price are snapshotted into the order item.
- Subtotal, tax, and total are calculated server-side.

### 6. Show Inventory Deduction

```bash
curl http://localhost:8080/products/1
```

Expected demo observation:

- `quantityAvailable` changed from `10` to `8`.

Narration:

- Inventory deduction happens in the same transaction as order creation.
- Failed order attempts do not deduct inventory.

### 7. Ship The Order

```bash
curl -u admin:admin-password -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

Narration:

- Shipping is a business operation, not a simple field update.
- Shipment creation requires `ADMIN`.
- The operation creates a shipment record and changes the order status.

### 8. Show Order Status Changed To SHIPPED

```bash
curl http://localhost:8080/orders/1
```

Expected demo observation:

- `status` is `SHIPPED`.
- The order still contains the original product snapshots.

### 9. Trigger A Duplicate Shipment Error

```bash
curl -u admin:admin-password -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

Expected demo observation:

- The API returns `409 Conflict`.
- The error response follows the standard API error shape.
- The system does not create a second shipment.

### 10. Run Tests

```bash
mvn test
```

Narration:

- Automated tests are the main proof of behavior.
- The suite covers unit, controller integration, functional workflow, destructive resilience, diagnostics, and security behavior.
- k6 scripts are separate performance smoke tests and do not replace Java tests.

### 11. Show GitHub Actions Passing

Open the repository on GitHub and show:

- The README CI badge.
- The `.github/workflows/ci.yml` file.
- A successful workflow run.

Narration:

- CI runs on push and pull request.
- CI sets up Java 21, caches Maven dependencies, runs tests, packages the app, and verifies the jar.

### 12. Show Docker Compose Running

```bash
docker compose up --build
```

In a second terminal:

```bash
curl http://localhost:8080/actuator/health
```

Stop containers after the demo:

```bash
docker compose down
```

Narration:

- Docker Compose runs the app with PostgreSQL.
- Local development still defaults to H2.
- This shows the service can run outside the IDE.

## Interview Talking Points

- I built this project phase by phase and kept each phase commit-ready.
- I separated controller, DTO, service, repository, entity, mapper, exception, and security concerns.
- I used automated tests as the source of truth instead of relying on manual curl commands.
- I modeled realistic business rules: unique SKUs, active product checks, inventory validation, order item snapshots, totals, inventory deduction, and shipment state transitions.
- I added resilience tests for invalid payloads, malformed JSON, duplicate operations, invalid IDs, and bad state transitions.
- I kept H2 for fast local development while adding PostgreSQL for Docker Compose.
- I added CI, security, logging, health checks, diagnostics, and performance smoke scripts to show production awareness.

## What To Avoid Overclaiming

- This is not a production identity system; users are in-memory demo users.
- The tax calculation is simplified for portfolio purposes.
- k6 baseline expectations are local smoke-test expectations, not production SLOs.
- Docker Compose is local runtime support, not a production deployment.
- GitHub Actions builds, tests, and packages; it does not deploy.

## Definition Of Done

- README is portfolio-ready.
- Architecture, testing, troubleshooting, API examples, and final summary docs are updated.
- Final demo script is documented here.
- `mvn test` passes.
- The phase is ready for a final Git commit.

## Git Commit

```bash
git add .
git commit -m "Phase 12: finalize documentation and portfolio polish"
```
