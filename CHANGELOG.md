# Changelog

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
