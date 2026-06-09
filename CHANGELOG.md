# Changelog

## Phase 11 - Performance Testing

- Added k6 performance smoke scripts under `performance/k6`.
- Added product list performance script for repeated `GET /products` traffic.
- Added order workflow performance script covering product create, order create, order read, and shipment.
- Added baseline expectations for request volume, average response time, p95 response time, and error rate.
- Documented that k6 scripts are separate from Maven tests.
- Updated README and project documentation.

## Phase 10 - Basic Security Exposure

- Added Spring Security dependency.
- Added HTTP Basic security configuration.
- Added in-memory demo users for `USER` and `ADMIN` roles.
- Protected product write endpoints with `ADMIN`.
- Protected order creation with `USER` or `ADMIN`.
- Protected shipment creation with `ADMIN`.
- Kept health and diagnostic endpoints public.
- Added automated security integration tests.
- Updated README and project documentation.

## Phase 9 - Logging, Monitoring, Health Checks, and Diagnostics

- Added structured key-value service logs for product creation, order creation, inventory deduction, shipment creation, and selected business failures.
- Added warning logs for duplicate SKU, inactive product, insufficient inventory, and invalid shipment state attempts.
- Exposed Actuator `health` and `info` endpoints.
- Enabled liveness and readiness health probes.
- Added application metadata under `/actuator/info`.
- Added automated tests for diagnostic endpoints and representative log output.
- Updated README and project documentation.

## Phase 8 - GitHub Actions CI/CD

- Added GitHub Actions CI workflow at `.github/workflows/ci.yml`.
- Configured CI to run on push and pull request events.
- Configured Java 21 setup with Maven dependency caching.
- Added CI steps for `mvn test` and `mvn package`.
- Added packaged jar verification.
- Added CI badge to README.
- Updated README and project documentation.

## Phase 7 - Docker and PostgreSQL

- Added `Dockerfile`.
- Added `.dockerignore`.
- Added `docker-compose.yml` with app and PostgreSQL services.
- Added `application-docker.yml` for the PostgreSQL-backed Docker profile.
- Added PostgreSQL JDBC runtime dependency.
- Preserved H2 as the default local development database.
- Documented Docker build, Compose startup, verification, and shutdown commands.
- Updated README and project documentation.

## Phase 6 - Destructive and Resilience Testing

- Added `DestructiveApiIntegrationTest`.
- Added destructive coverage for malformed JSON, invalid product payloads, invalid order payloads, invalid shipment payloads, invalid path IDs, and nonexistent resources.
- Added duplicate SKU and duplicate shipment resilience checks.
- Added inventory edge case coverage for insufficient inventory and inactive products.
- Added invalid order state coverage for cancelled order shipment attempts.
- Verified failed requests do not create unwanted persisted state.
- Documented the focused destructive test command and troubleshooting notes.

## Phase 5 - Integration and Functional Testing

- Added `OrderWorkflowIntegrationTest`.
- Added full product-to-order-to-shipment workflow coverage.
- Verified inventory deduction, order totals, item snapshots, shipment persistence, and shipped status in one end-to-end flow.
- Added failure workflow coverage for insufficient inventory, duplicate shipment, and invalid product payloads.
- Documented the focused workflow test command.
- Updated README and project documentation.

## Phase 4 - Error Handling and Validation

- Added `ErrorResponse` DTO for standard API error payloads.
- Added centralized `GlobalExceptionHandler`.
- Added shared `ResourceNotFoundException` base for missing resources.
- Standardized validation, malformed JSON, not found, conflict, and unexpected server error responses.
- Added field-level validation error details.
- Added API integration tests for important error response behavior.
- Added handler unit coverage for unexpected server errors.
- Updated README and project documentation.

## Phase 3 - Shipment Workflow

- Added `Shipment` JPA entity.
- Added Spring Data `ShipmentRepository`.
- Added ship order request DTO and shipment response DTO.
- Added shipment mapper.
- Added order shipping business operation to `OrderService`.
- Added `POST /orders/{id}/ship` endpoint.
- Added invalid order state validation for already shipped and cancelled orders.
- Added shipment service unit coverage and API integration tests.
- Updated README and project documentation.

## Phase 2 - Order Workflow

- Added `Order` and `OrderItem` JPA entities.
- Added `OrderStatus` enum.
- Added Spring Data repositories for orders and order items.
- Added order create request DTOs and order response DTOs.
- Added order service business rules for product lookup, active product validation, inventory validation, snapshots, totals, and inventory deduction.
- Added REST endpoints for order list, lookup, and creation.
- Added fixed `8.25%` tax calculation for portfolio/demo purposes.
- Added order service unit tests and API integration tests.
- Updated README and project documentation.

## Phase 1 - Product API

- Added `Product` JPA entity with SKU, name, description, price, inventory quantity, active status, and audit timestamps.
- Added Spring Data `ProductRepository`.
- Added product create/update request DTOs and product response DTO.
- Added product service business rules for unique SKU, price, quantity, lookup, update, and soft delete.
- Added REST endpoints for product list, lookup, create, update, and deactivate.
- Enabled the H2 console for local database inspection.
- Added service unit tests and MockMvc API tests for product behavior.
- Updated README and project documentation.

## Phase 0 - Project Setup

- Initialized Java 21 Spring Boot 3 Maven project baseline.
- Added Spring Web, Spring Data JPA, H2, Validation, Actuator, and test dependencies.
- Added custom `GET /health` endpoint and Actuator health exposure.
- Added H2 local datasource configuration.
- Added starter tests and documentation.
