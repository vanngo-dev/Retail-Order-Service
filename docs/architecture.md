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
- `security`: security configuration planned for a later phase

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
