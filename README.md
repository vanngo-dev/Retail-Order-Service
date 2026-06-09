# Retail Order Service

Retail Order Service is a production-style Java/Spring Boot backend API that models a simplified retail order workflow. The project is built phase by phase to demonstrate backend engineering skills in REST API development, SQL persistence, validation, testing, source control, and production readiness.

## Tech Stack

- Java 21
- Spring Boot 3.x
- Maven
- Spring Web
- Spring Data JPA
- H2 Database
- PostgreSQL
- Spring Validation
- Spring Boot Actuator
- JUnit 5 and Spring Boot Test
- Docker and Docker Compose

## Phase 0 - Project Setup

Completed baseline Spring Boot project setup with Java 21, Maven, Spring Web, JPA, H2, Validation, Actuator, and test dependencies.

This phase includes:

- Spring Boot application entrypoint under `com.example.retailorderservice`
- Base package structure for controllers, DTOs, entities, repositories, services, mappers, exceptions, config, and security
- In-memory H2 datasource configuration for local development
- Actuator health endpoint at `GET /actuator/health`
- Custom service health endpoint at `GET /health`
- Starter tests for application context, custom health, and Actuator health
- Starter documentation and Git-ready project hygiene

## Phase 1 - Product API

Implemented product management with JPA persistence, validation, DTOs, service layer, controller layer, unique SKU handling, and soft delete behavior.

This phase includes:

- `Product` JPA entity with SKU, name, description, price, available quantity, active status, and audit timestamps
- `ProductRepository` using Spring Data JPA
- Request and response DTOs for create, update, and read operations
- `ProductService` business rules for SKU uniqueness, price validation, quantity validation, lookup, update, and deactivation
- `ProductController` REST endpoints under `/products`
- H2 console enabled for local database inspection at `/h2-console`
- Unit tests for product service business rules
- Spring Boot integration tests for the Product API in `ProductApiIntegrationTest`

## Phase 2 - Order Workflow

Implemented order creation with order items, product lookup, inventory validation, price/name/SKU snapshots, subtotal/tax/total calculation, and inventory deduction.

This phase includes:

- `Order` and `OrderItem` JPA entities
- `OrderStatus` enum with lifecycle statuses
- `OrderRepository` and `OrderItemRepository`
- Request and response DTOs for order creation and lookup
- `OrderService` business rules for product lookup, active product checks, inventory validation, snapshotting, totals, and inventory deduction
- `OrderController` REST endpoints under `/orders`
- Fixed `8.25%` tax calculation for portfolio/demo purposes
- Unit tests for order service business rules
- Spring Boot integration tests for the Order API in `OrderApiIntegrationTest`

## Phase 3 - Shipment Workflow

Implemented shipment creation and order status transition from `CREATED` to `SHIPPED`. Added validation to prevent duplicate shipments and invalid order state transitions.

This phase includes:

- `Shipment` JPA entity with order reference, carrier, tracking number, shipped time, and creation timestamp
- `ShipmentRepository` using Spring Data JPA
- Request and response DTOs for shipping an order
- `OrderService` shipping business operation
- `POST /orders/{id}/ship` endpoint
- Validation for missing carrier, missing tracking number, nonexistent orders, already shipped orders, and cancelled orders
- Unit tests for shipment business rules
- Spring Boot integration tests for the Shipment API in `ShipmentApiIntegrationTest`

## Phase 4 - Error Handling and Validation

Added centralized exception handling, consistent error responses, validation messages, and proper HTTP status codes for API failures.

This phase includes:

- `ErrorResponse` DTO for predictable API error payloads
- `GlobalExceptionHandler` for centralized exception mapping
- Shared `ResourceNotFoundException` base for missing resources
- Standard responses for validation errors, malformed JSON, missing resources, duplicate SKU, insufficient inventory, invalid order state, duplicate shipment, and unexpected server errors
- Field-level validation details for request validation failures
- Automated error response tests in `ApiErrorIntegrationTest` and `GlobalExceptionHandlerTest`

## Phase 5 - Integration and Functional Testing

Added unit, integration, and functional workflow tests covering product creation, order creation, inventory deduction, shipment creation, validation errors, and invalid state transitions.

This phase includes:

- Full product-to-order-to-shipment workflow coverage in `OrderWorkflowIntegrationTest`
- End-to-end API tests using Spring Boot, MockMvc, services, repositories, validation, and H2 persistence together
- Repository state assertions for inventory deduction, order creation, shipment creation, and failed workflow rollback behavior
- Failure workflow tests proving invalid product payloads, insufficient inventory, and duplicate shipments return predictable errors without unwanted persistence changes
- Documentation for running all tests or the focused workflow suite

Manual workflow demo commands live in `docs/youtube/05-integration-functional-testing.md`.

## Phase 6 - Destructive and Resilience Testing

Added destructive tests that intentionally send invalid payloads, bad IDs, duplicate operations, insufficient inventory cases, and invalid order state transitions. These tests demonstrate how the API fails safely and predictably.

This phase includes:

- Destructive API coverage in `DestructiveApiIntegrationTest`
- Malformed JSON, missing fields, invalid values, and invalid path ID tests
- Duplicate SKU and duplicate shipment tests
- Nonexistent product and order ID tests
- Inactive product and insufficient inventory tests
- Cancelled order shipment tests
- State-safety assertions proving failed requests do not create unwanted products, orders, order items, shipments, or inventory deductions
- Timeout and error troubleshooting notes

Manual destructive demo commands live in `docs/youtube/06-destructive-testing.md`.

## Phase 7 - Docker and PostgreSQL

Added Docker support and a PostgreSQL-backed Docker Compose profile. The application can run locally with H2 or in containers with PostgreSQL.

This phase includes:

- `Dockerfile` for building and running the Spring Boot jar
- `.dockerignore` for cleaner Docker build context
- `docker-compose.yml` with `retail-order-service` and `postgres` services
- `application-docker.yml` Spring profile for PostgreSQL
- PostgreSQL JDBC driver as a runtime dependency
- H2 preserved as the default local development database
- Documentation for Docker build, Compose startup, health checks, and shutdown

Manual Docker demo commands live in `docs/youtube/07-docker-postgresql.md`.

## Package Structure

```text
com.example.retailorderservice
|-- config
|-- controller
|-- dto
|   |-- request
|   `-- response
|-- entity
|-- exception
|-- mapper
|-- repository
|-- security
`-- service
```

## Endpoints

### Custom Health

```http
GET /health
```

Example response:

```json
{
  "status": "UP",
  "service": "retail-order-service",
  "timestamp": "2026-06-04T12:00:00Z"
}
```

### Actuator Health

```http
GET /actuator/health
```

Example response:

```json
{
  "status": "UP"
}
```

### Products

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/products` | List products |
| GET | `/products/{id}` | Get product by ID |
| POST | `/products` | Create product |
| PUT | `/products/{id}` | Update product |
| DELETE | `/products/{id}` | Deactivate product |

`GET /products` supports optional query parameters:

| Parameter | Example | Purpose |
|---|---|---|
| `active` | `true` | Filter by active status |
| `sku` | `HAMMER-001` | Filter by SKU |
| `page` | `0` | Select result page |
| `size` | `20` | Select page size |

Detailed manual demo commands live in `docs/youtube/01-product-api.md`. Reusable API request examples live in `docs/api-examples.md`.

### Orders

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/orders` | List orders |
| GET | `/orders/{id}` | Get order by ID |
| POST | `/orders` | Create order |
| POST | `/orders/{id}/ship` | Ship an order |

`GET /orders` supports optional query parameters:

| Parameter | Example | Purpose |
|---|---|---|
| `status` | `CREATED` | Filter by order status |
| `customerEmail` | `customer@example.com` | Filter by customer email |
| `page` | `0` | Select result page |
| `size` | `20` | Select page size |

Order totals are calculated server-side. Phase 2 uses a simplified fixed `8.25%` tax rate for portfolio purposes.

Shipping is a business operation. It creates a shipment record and changes the order status from `CREATED` to `SHIPPED`.

Detailed manual demo commands live in `docs/youtube/02-order-workflow.md` and `docs/youtube/03-shipment-workflow.md`. Reusable API request examples live in `docs/api-examples.md`.

### Error Responses

API failures use a consistent response shape:

```json
{
  "timestamp": "2026-06-04T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Quantity must be greater than zero",
  "path": "/orders"
}
```

Validation failures may also include `validationErrors` with field-specific messages.

Detailed manual demo commands live in `docs/youtube/04-error-handling-validation.md`. Reusable examples live in `docs/api-examples.md`.

## How to Run

Local H2 mode:

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

Local H2 console:

```text
http://localhost:8080/h2-console
```

Use JDBC URL `jdbc:h2:mem:retail_order_service`, user `sa`, and a blank password.

## How to Run with Docker

Build the image:

```bash
docker build -t retail-order-service .
```

Start the app with PostgreSQL:

```bash
docker compose up --build
```

Verify:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/products
```

Stop the containers:

```bash
docker compose down
```

Docker Compose runs the app with the `docker` Spring profile and PostgreSQL. Local `mvn spring-boot:run` still uses H2 by default.

## How to Test

```bash
mvn test
```

Run only the Phase 5 workflow tests:

```bash
mvn test -Dtest=OrderWorkflowIntegrationTest
```

Run only the Phase 6 destructive tests:

```bash
mvn test -Dtest=DestructiveApiIntegrationTest
```

## Phase Completion Rule

No phase is complete until the app runs, tests pass, documentation is updated, and the phase is ready for a Git commit.

Definition of Done:

- `mvn test` passes.
- Automated tests cover the phase behavior.
- Manual curl demo commands are documented in the matching YouTube tutorial file.
