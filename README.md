# Retail Order Service

Retail Order Service is a production-style Java/Spring Boot backend API that models a simplified retail order workflow. The project is built phase by phase to demonstrate backend engineering skills in REST API development, SQL persistence, validation, testing, source control, and production readiness.

## Tech Stack

- Java 21
- Spring Boot 3.x
- Maven
- Spring Web
- Spring Data JPA
- H2 Database
- Spring Validation
- Spring Boot Actuator
- JUnit 5 and Spring Boot Test

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

## How to Run

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

Local H2 console:

```text
http://localhost:8080/h2-console
```

Use JDBC URL `jdbc:h2:mem:retail_order_service`, user `sa`, and a blank password.

## How to Test

```bash
mvn test
```

## Phase Completion Rule

No phase is complete until the app runs, tests pass, documentation is updated, and the phase is ready for a Git commit.

Definition of Done:

- `mvn test` passes.
- Automated tests cover the phase behavior.
- Manual curl demo commands are documented in the matching YouTube tutorial file.
