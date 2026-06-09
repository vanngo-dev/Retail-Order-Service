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

## How to Run

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

## How to Test

```bash
mvn test
```

## Phase Completion Rule

No phase is complete until the app runs, tests pass, documentation is updated, and the phase is ready for a Git commit.
