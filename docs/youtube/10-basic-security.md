# Basic Security Exposure Notes

## Goal

Build Phase 10 of the retail order service by adding basic Spring Security authentication and role-based authorization to demonstrate security awareness for backend services.

## What We Built

- Spring Security dependency.
- HTTP Basic authentication.
- In-memory demo users.
- `USER` and `ADMIN` role distinction.
- Public health and diagnostic endpoints.
- Protected product write endpoints.
- Protected order creation endpoint.
- Protected shipment endpoint.
- Automated security integration tests.

## Why It Matters

Security is not only about logging in. A backend service also needs to decide what an authenticated user is allowed to do. Role-based authorization maps well to business systems because different users naturally have different responsibilities.

## Code Concepts

- Authentication answers: who are you?
- Authorization answers: what are you allowed to do?
- `USER` can create orders.
- `ADMIN` can manage products and ship orders.
- Public health endpoints stay open so uptime checks can work.
- This phase uses simple in-memory users for portfolio demonstration only.

## Files Changed

- `pom.xml`
- `src/main/java/com/example/retailorderservice/security/SecurityConfig.java`
- `src/test/java/com/example/retailorderservice/security/SecurityIntegrationTest.java`
- Existing API integration tests authenticated with the correct role for protected write operations.
- `README.md`
- `docs/testing-guide.md`
- `docs/architecture.md`
- `docs/troubleshooting.md`
- `docs/youtube/10-basic-security.md`
- `CHANGELOG.md`

## Demo Users

| Username | Password | Roles |
|---|---|---|
| `user` | `user-password` | `USER` |
| `admin` | `admin-password` | `USER`, `ADMIN` |

These users are intentionally simple demo users. This phase does not implement JWT, OAuth, database-backed users, or frontend login.

## Security Rules

| Endpoint | Access |
|---|---|
| `GET /health` | Public |
| `GET /actuator/health` | Public |
| `GET /actuator/info` | Public |
| `GET /products/**` | Public |
| `GET /orders/**` | Public |
| `POST /products` | `ADMIN` |
| `PUT /products/{id}` | `ADMIN` |
| `DELETE /products/{id}` | `ADMIN` |
| `POST /orders` | `USER` or `ADMIN` |
| `POST /orders/{id}/ship` | `ADMIN` |

## How to Run

Run the app locally:

```bash
mvn spring-boot:run
```

## Manual Security Demo Flow

These curl commands are for the video/demo walkthrough. Automated tests in `src/test/java/...` are the verification method for this phase.

Show public health access:

```bash
curl http://localhost:8080/actuator/health
```

Show anonymous product create is blocked:

```bash
curl -i -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SEC-HAMMER-001",
    "name": "Security Demo Hammer",
    "description": "Product used to demonstrate authorization",
    "price": 19.99,
    "quantityAvailable": 10,
    "active": true
  }'
```

Show `USER` cannot create a product:

```bash
curl -i -u user:user-password -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SEC-HAMMER-001",
    "name": "Security Demo Hammer",
    "description": "Product used to demonstrate authorization",
    "price": 19.99,
    "quantityAvailable": 10,
    "active": true
  }'
```

Show `ADMIN` can create a product:

```bash
curl -i -u admin:admin-password -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SEC-HAMMER-001",
    "name": "Security Demo Hammer",
    "description": "Product used to demonstrate authorization",
    "price": 19.99,
    "quantityAvailable": 10,
    "active": true
  }'
```

Show `USER` can create an order. Replace `PRODUCT_ID` with the product ID from the admin create response:

```bash
curl -i -u user:user-password -X POST http://localhost:8080/orders \
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

Show `USER` cannot ship the order. Replace `ORDER_ID` with the order ID from the order create response:

```bash
curl -i -u user:user-password -X POST http://localhost:8080/orders/ORDER_ID/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

Show `ADMIN` can ship the order:

```bash
curl -i -u admin:admin-password -X POST http://localhost:8080/orders/ORDER_ID/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

## How to Test

Run the automated test suite:

```bash
mvn test
```

Run only the security tests:

```bash
mvn test -Dtest=SecurityIntegrationTest
```

## Common Errors

- `401 Unauthorized`: credentials are missing or invalid.
- `403 Forbidden`: credentials are valid, but the user does not have the required role.
- Product write fails for `USER`: use `ADMIN`.
- Shipment fails for `USER`: use `ADMIN`.
- Health checks fail unexpectedly: health endpoints should be public.

## Interview Talking Points

- Explain the difference between authentication and authorization.
- Explain why write endpoints need stricter protection than read endpoints.
- Explain why role-based access maps to business responsibilities.
- Explain why health endpoints often remain public or separately managed.
- Explain why this phase uses simple demo users instead of JWT, OAuth, or database-backed users.

## Definition of Done

- `mvn test` passes.
- Public health endpoints remain accessible without authentication.
- Protected write endpoints require authentication.
- Role-based access is enforced for `USER` and `ADMIN`.
- Automated tests prove security behavior.
- Manual security curl commands are documented in this YouTube tutorial file.

## Commit Message

```bash
git commit -m "Phase 10: add basic Spring Security authorization"
```
