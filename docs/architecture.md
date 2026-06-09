# Architecture

## Layered Application

Retail Order Service is a layered Spring Boot application under the `com.example.retailorderservice` package.

Layers:

- `controller`: REST API entrypoints
- `dto.request`: inbound API payloads
- `dto.response`: outbound API payloads
- `service`: business workflow logic
- `repository`: Spring Data persistence access
- `entity`: JPA entities
- `mapper`: conversions between entities and DTOs
- `exception`: API and business exception handling
- `config`: application configuration
- `security`: Spring Security configuration and role-based endpoint protection

## Final Architecture Snapshot

Request flow:

```text
HTTP request
  -> Controller
  -> Validated request DTO
  -> Service business workflow
  -> Repository
  -> JPA entity
  -> Mapper
  -> Response DTO
  -> HTTP response
```

Cross-cutting concerns:

- Bean Validation protects request payloads before service logic runs.
- `GlobalExceptionHandler` turns validation and business failures into consistent API errors.
- Spring Security protects write operations while leaving health, diagnostics, and read endpoints public for the demo.
- Service methods own transaction boundaries for order creation, inventory deduction, and shipment creation.
- Actuator, structured logs, GitHub Actions, Docker Compose, and k6 scripts demonstrate production support exposure around the core API.

## Database Model

| Entity | Table | Relationship |
|---|---|---|
| `Product` | `products` | Referenced by order items through a snapshotted `productId` value |
| `Order` | `orders` | Owns many `OrderItem` rows |
| `OrderItem` | `order_items` | Belongs to one order and stores product SKU, name, and price snapshots |
| `Shipment` | `shipments` | One-to-one with an order |

Important modeling choices:

- Product deletes are soft deletes by setting `active` to `false`.
- Order items snapshot product data so historical orders stay stable when products change.
- Order totals are calculated server-side with a simplified fixed `8.25%` demo tax rate.
- Shipping creates a shipment row and changes order status in one operation.
- The default local database is H2; Docker Compose uses PostgreSQL through the `docker` Spring profile.

## Phase 0 Baseline

Phase 0 includes the application shell, local H2 configuration, package structure, and health endpoints.

## Phase 1 Product API

Phase 1 adds product management with SQL persistence:

- `Product` is the first JPA entity.
- `ProductRepository` owns database access.
- Product request DTOs validate inbound API payloads.
- `ProductResponse` keeps API output separate from persistence details.
- `ProductService` owns SKU uniqueness, price validation, quantity validation, lookup, update, and deactivation rules.
- `ProductController` exposes REST endpoints under `/products`.

Delete operations are soft deletes: the row remains in the database and `active` is set to `false`.

## Phase 2 Order Workflow

Phase 2 adds order creation with line items:

- `Order` stores customer email, order number, status, subtotal, tax, total, and audit timestamps.
- `OrderItem` stores product ID plus product SKU, name, and unit price snapshots.
- `OrderService` validates products, active status, inventory availability, item quantities, and customer email.
- The service calculates line totals, subtotal, fixed `8.25%` tax, and total server-side.
- Product inventory is deducted in the same transaction as order creation.
- Order lookup, listing, and creation are exposed under `/orders`.

## Phase 3 Shipment Workflow

Phase 3 adds shipping as an order business operation:

- `Shipment` stores the order reference, carrier, tracking number, shipped time, and creation timestamp.
- One order can have one shipment in the MVP.
- `OrderService` validates the order exists and is still in `CREATED` status.
- Shipping creates a shipment record and changes order status to `SHIPPED` in one transaction.
- Already shipped orders and cancelled orders are rejected.
- The shipment endpoint is exposed at `POST /orders/{id}/ship`.

## Phase 4 Error Handling and Validation

Phase 4 centralizes API failure handling:

- `ErrorResponse` defines the standard error payload with timestamp, status, error, message, path, and optional validation details.
- `GlobalExceptionHandler` maps validation failures, malformed JSON, missing resources, duplicate SKU, inventory conflicts, invalid order states, duplicate shipments, and unexpected exceptions.
- `ResourceNotFoundException` is the shared base type for missing API resources.
- Domain exceptions stay focused on business meaning while HTTP status mapping lives in one handler.
- Bean Validation messages are returned to clients with field-level `validationErrors` where applicable.

## Phase 5 Integration and Functional Testing

Phase 5 strengthens confidence in the layered architecture:

- `OrderWorkflowIntegrationTest` runs full API workflows through controllers, services, repositories, validation, and H2 persistence.
- Functional tests cover the user journey from product creation to order creation, inventory deduction, shipment creation, and final shipped status.
- Failure workflow tests verify invalid requests and business conflicts return predictable errors without unwanted persistence changes.
- Unit tests still cover isolated service rules, while integration and functional tests prove the layers work together.

## Phase 6 Destructive and Resilience Testing

Phase 6 proves the API fails safely:

- `DestructiveApiIntegrationTest` intentionally sends bad JSON, invalid payloads, invalid IDs, duplicate operations, inventory conflicts, inactive product orders, and invalid shipment state transitions.
- Tests assert both the API error response and the persisted state after failure.
- Failed requests should not create unwanted products, orders, order items, shipments, or inventory deductions.
- Resilience coverage stays test-focused; no Docker, CI, security, or performance infrastructure is introduced in this phase.

## Phase 7 Docker and PostgreSQL Profile

Phase 7 adds a second runtime mode without changing local defaults:

- Local development uses `application.yml` with in-memory H2.
- Docker Compose uses `application-docker.yml` with PostgreSQL.
- The `retail-order-service` container starts with `SPRING_PROFILES_ACTIVE=docker`.
- The `postgres` service provides the database for containerized runs.
- H2 console remains available in local mode and is disabled in the Docker profile.
- Docker support is runtime packaging only; CI build and package checks are added in Phase 8, while production deployment remains out of scope.

## Phase 8 GitHub Actions CI/CD

Phase 8 adds automated repository checks:

- `.github/workflows/ci.yml` runs on push and pull request events.
- The workflow checks out the repository and sets up Temurin Java 21.
- Maven dependency caching speeds up repeated workflow runs.
- CI runs `mvn test` to prove the automated test suite.
- CI runs `mvn package` and verifies the Spring Boot jar exists.
- The workflow stops at build, test, and package verification; it does not deploy the application.

## Phase 9 Logging and Monitoring

Phase 9 adds basic production diagnostics:

- Services emit structured key-value logs for product creation, order creation, inventory deduction, shipment creation, and selected business failures.
- Successful business events use `INFO`.
- Business conflicts that help root-cause failed workflows use `WARN`.
- Logs avoid unnecessary sensitive values such as customer email and shipment tracking numbers.
- Actuator exposes `health` and `info` endpoints.
- Health probes expose liveness and readiness status for container and cloud-style runtime checks.
- `/actuator/info` returns stable service metadata from application configuration.
- Diagnostics do not change business API behavior.

## Phase 10 Basic Security

Phase 10 adds a simple Spring Security boundary:

- `SecurityConfig` defines HTTP Basic authentication and role-based authorization.
- In-memory demo users represent `USER` and `ADMIN` roles.
- Health and diagnostic endpoints remain public for uptime checks.
- Product and order read endpoints remain public for portfolio/demo workflows.
- Product write endpoints require `ADMIN`.
- Order creation requires `USER` or `ADMIN`.
- Shipment creation requires `ADMIN`.
- CSRF is disabled for this stateless REST API demo.
- H2 console access remains available for local development.

This phase intentionally avoids JWT, OAuth, database-backed users, frontend login, and production identity management.

## Phase 11 Performance Testing

Phase 11 adds performance testing artifacts outside the Spring Boot application:

- k6 scripts live under `performance/k6`.
- `product-list.js` exercises repeated product list reads.
- `order-workflow.js` exercises product creation, order creation, order lookup, and shipment creation.
- Scripts use the Phase 10 HTTP Basic demo users for protected write operations.
- Baseline expectations document request volume, average response time, p95 response time, and error rate.
- Performance scripts are operational test assets and do not change application business behavior.

## Phase 12 Final Portfolio Polish

Phase 12 turns the completed service into a presentable portfolio project:

- README is organized around architecture, workflow, endpoints, runtime modes, testing, security, observability, CI/CD, troubleshooting, lessons learned, and future improvements.
- A final demo script documents the complete product-to-order-to-shipment walkthrough.
- Portfolio wording explains the project for GitHub, resume bullets, LinkedIn, and interview discussion.
- Documentation is cleaned up so protected write examples use the Phase 10 demo credentials.
