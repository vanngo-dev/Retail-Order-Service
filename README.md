# Retail Order Service

[![CI](https://github.com/vanngo-dev/Retail-Order-Service/actions/workflows/ci.yml/badge.svg)](https://github.com/vanngo-dev/Retail-Order-Service/actions/workflows/ci.yml)

## Overview

Retail Order Service is a production-style Java/Spring Boot backend API that models a simplified retail workflow for products, orders, inventory, and shipments.

This project demonstrates my ability to build and support production-style backend services using Java, Spring Boot, SQL, REST APIs, validation, testing, Docker, CI/CD, logging, monitoring, and security fundamentals. The domain models a realistic retail workflow with products, orders, inventory, and shipments, making it relevant to business software and retail technology environments.

## Why This Project Exists

This project was built as a portfolio-ready backend service for Software Engineer II applications, technical interviews, GitHub review, and YouTube/demo walkthroughs. It is intentionally scoped as a realistic service rather than a toy CRUD app: orders depend on product state, inventory is validated and deducted, shipment is a business operation, and failure cases are covered with automated tests.

The work was completed in phases so each milestone stayed runnable, tested, documented, and ready for a Git commit before the next phase began.

## Tech Stack

- Java 21
- Spring Boot 3.3.x
- Maven
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Security
- Spring Boot Actuator
- H2 for default local development
- PostgreSQL for Docker Compose
- JUnit 5, Spring Boot Test, MockMvc, and Spring Security Test
- SLF4J and Logback logging
- Docker and Docker Compose
- GitHub Actions
- k6 performance scripts

## Architecture

The application uses a layered Spring Boot architecture under `com.example.retailorderservice`.

| Layer | Responsibility |
|---|---|
| `controller` | REST endpoints and HTTP response handling |
| `dto.request` | Validated inbound request payloads |
| `dto.response` | API response payloads separated from persistence entities |
| `service` | Product, order, inventory, shipment, and business workflow rules |
| `repository` | Spring Data JPA database access |
| `entity` | JPA persistence model |
| `mapper` | Entity-to-response conversion |
| `exception` | Centralized API error handling |
| `security` | HTTP Basic authentication and role-based authorization |

Architecture details live in `docs/architecture.md`.

## Business Workflow

The core workflow is:

1. Create an active product with a unique SKU and available inventory.
2. Create an order for one or more products.
3. Validate that each product exists, is active, and has enough inventory.
4. Snapshot product SKU, name, and unit price into order items.
5. Calculate subtotal, fixed demo tax, and total server-side.
6. Deduct inventory in the same transaction as order creation.
7. Ship the order as a business operation.
8. Create one shipment record and transition the order from `CREATED` to `SHIPPED`.

Duplicate SKUs, inactive products, insufficient inventory, duplicate shipments, invalid order states, malformed JSON, and validation failures return predictable API errors.

## Database Model

| Table | Purpose | Key Fields |
|---|---|---|
| `products` | Product catalog and inventory | `sku`, `name`, `price`, `quantity_available`, `active`, audit timestamps |
| `orders` | Customer order header | `order_number`, `customer_email`, `status`, `subtotal`, `tax`, `total`, audit timestamps |
| `order_items` | Snapshotted line items | `order_id`, `product_id`, `sku_snapshot`, `product_name_snapshot`, `unit_price_snapshot`, `quantity`, `line_total` |
| `shipments` | Shipment record for an order | `order_id`, `carrier`, `tracking_number`, `shipped_at`, `created_at` |

Local development uses an in-memory H2 database by default. Docker Compose runs the same application against PostgreSQL.

## API Endpoints

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/health` | Public | Custom service health |
| `GET` | `/actuator/health` | Public | Actuator health |
| `GET` | `/actuator/health/liveness` | Public | Liveness probe |
| `GET` | `/actuator/health/readiness` | Public | Readiness probe |
| `GET` | `/actuator/info` | Public | Service metadata |
| `GET` | `/products` | Public | List products with optional filters |
| `GET` | `/products/{id}` | Public | Get one product |
| `POST` | `/products` | `ADMIN` | Create product |
| `PUT` | `/products/{id}` | `ADMIN` | Update product |
| `DELETE` | `/products/{id}` | `ADMIN` | Deactivate product |
| `GET` | `/orders` | Public | List orders with optional filters |
| `GET` | `/orders/{id}` | Public | Get one order |
| `POST` | `/orders` | `USER` or `ADMIN` | Create order |
| `POST` | `/orders/{id}/ship` | `ADMIN` | Ship order |

Reusable request examples live in `docs/api-examples.md`. Step-by-step demo commands live in the matching files under `docs/youtube/`.

## How to Run Locally

Start the application with H2:

```bash
mvn spring-boot:run
```

The application runs at `http://localhost:8080`.

The local H2 console is available at:

```text
http://localhost:8080/h2-console
```

Use:

```text
JDBC URL: jdbc:h2:mem:retail_order_service
User: sa
Password:
```

The password is blank, and H2 data resets when the app restarts.

## How to Run with Docker

Build and run the application with PostgreSQL:

```bash
docker compose up --build
```

Stop the containers:

```bash
docker compose down
```

Docker Compose starts PostgreSQL plus the Spring Boot app with `SPRING_PROFILES_ACTIVE=docker`. H2 remains the default for local `mvn spring-boot:run`.

## How to Run Tests

Run the full Java test suite:

```bash
mvn test
```

Run focused suites:

```bash
mvn test -Dtest=OrderWorkflowIntegrationTest
mvn test -Dtest=DestructiveApiIntegrationTest
mvn test -Dtest=SecurityIntegrationTest
mvn test -Dtest=HealthControllerTest
```

Package the application:

```bash
mvn package
```

The Java suite is the source of truth for application behavior. k6 scripts are separate performance smoke scripts and do not replace Maven tests.

Testing details live in `docs/testing-guide.md`.

## Functional Workflow Demo

The final demo walkthrough shows:

1. Start the application.
2. Show health endpoint.
3. Create a product.
4. List products.
5. Create an order.
6. Show inventory deduction.
7. Ship the order.
8. Show order status changed to `SHIPPED`.
9. Trigger a duplicate shipment error.
10. Run tests.
11. Show GitHub Actions passing.
12. Show Docker Compose running.

The full command-by-command final demo script lives in `docs/youtube/12-final-demo-portfolio-polish.md`.

## Destructive Testing

Destructive and resilience tests intentionally verify bad input and bad state:

- Malformed JSON
- Missing fields
- Invalid values
- Duplicate SKU
- Inactive product order attempts
- Insufficient inventory
- Nonexistent product and order IDs
- Duplicate shipment attempts
- Cancelled order shipment attempts

These tests assert both the API response and the persisted state after failure.

## Performance Testing

k6 scripts live under `performance/k6`:

- `product-list.js` exercises repeated `GET /products` traffic.
- `order-workflow.js` exercises product creation, order creation, order lookup, and shipment.

Baseline expectations live in `performance/baseline-expectations.md`. Manual performance commands live in `docs/youtube/11-performance-testing.md`.

## Security

The project uses HTTP Basic authentication with simple in-memory demo users.

| Username | Password | Roles |
|---|---|---|
| `user` | `user-password` | `USER` |
| `admin` | `admin-password` | `USER`, `ADMIN` |

Public endpoints include health, diagnostics, product reads, and order reads. Product writes and shipment creation require `ADMIN`; order creation requires `USER` or `ADMIN`.

These credentials are for local portfolio/demo use only. The project intentionally does not implement JWT, OAuth, database-backed users, or a frontend login.

## Logging And Monitoring

Spring Boot Actuator exposes health, liveness, readiness, and service info endpoints. Services emit structured key-value logs for important business events and selected failure cases, including product creation, order creation, inventory deduction, shipment creation, insufficient inventory, and invalid shipment attempts.

Diagnostics details live in `docs/youtube/09-logging-monitoring-diagnostics.md`.

## CI/CD

GitHub Actions runs on push and pull request:

- Checks out the repository.
- Sets up Temurin Java 21.
- Caches Maven dependencies.
- Runs `mvn test`.
- Runs `mvn package`.
- Verifies the Spring Boot jar exists.

The workflow verifies build readiness but does not deploy infrastructure.

## Troubleshooting

Common local issues and fixes are documented in `docs/troubleshooting.md`, including Java and Maven setup, H2 console access, API validation failures, Docker Compose startup, GitHub Actions failures, security responses, and k6 availability.

## What I Learned

- How to grow a backend service one commit-ready phase at a time.
- How to separate REST controllers, DTOs, business services, repositories, entities, and exception handling.
- How to model realistic retail workflow rules such as inventory validation, product snapshots, and shipment state transitions.
- How to test the service at multiple levels: unit, controller integration, functional workflow, destructive resilience, diagnostics, and security.
- How to keep local development simple with H2 while supporting a PostgreSQL Docker profile.
- How to add CI, logging, health checks, security boundaries, and performance smoke tests without changing the core business API.

## Future Improvements

- Replace in-memory demo users with production identity such as OAuth2/OIDC or JWT validation.
- Add Flyway or Liquibase database migrations.
- Add Testcontainers for PostgreSQL-backed integration tests.
- Add OpenAPI documentation.
- Add metrics dashboards and alerting.
- Add idempotency keys for order and shipment operations.
- Add optimistic locking or inventory reservation logic for higher concurrency.
- Add a real deployment target such as Azure, AWS, GCP, or Kubernetes.
- Expand k6 scenarios after collecting real baseline numbers in a controlled environment.

## Documentation Map

- `docs/architecture.md` explains the system design.
- `docs/testing-guide.md` explains automated test coverage.
- `docs/api-examples.md` keeps reusable API request examples.
- `docs/troubleshooting.md` collects common debugging notes.
- `docs/portfolio-summary.md` provides resume, LinkedIn, and interview wording.
- `docs/youtube/` contains phase-by-phase tutorial and demo scripts.

## Phase Completion Rule

No phase is complete until the app runs, tests pass, documentation is updated, and the phase is ready for a Git commit.
