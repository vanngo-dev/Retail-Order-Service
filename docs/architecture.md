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

## Phase 2 Order Workflow

Phase 2 adds order creation with line items:

- `Order` stores customer email, order number, status, subtotal, tax, total, and audit timestamps.
- `OrderItem` stores product ID plus product SKU, name, and unit price snapshots.
- `OrderService` validates products, active status, inventory availability, item quantities, and customer email.
- The service calculates line totals, subtotal, fixed `8.25%` tax, and total server-side.
- Product inventory is deducted in the same transaction as order creation.
- Order lookup and listing are exposed under `/orders`.

Shipment state transitions remain planned for Phase 3 and are not implemented in Phase 2.
