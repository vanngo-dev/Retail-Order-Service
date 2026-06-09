# Architecture

## Phase 0 Baseline

Retail Order Service starts as a layered Spring Boot application under the `com.example.retailorderservice` package.

Planned layers:

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

Phase 0 includes only the application shell, local H2 configuration, and health endpoints. Product, order, and shipment workflows are added in later phases.
