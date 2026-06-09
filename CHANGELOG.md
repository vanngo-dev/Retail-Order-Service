# Changelog

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
