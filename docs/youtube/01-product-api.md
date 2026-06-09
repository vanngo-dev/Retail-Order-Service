# Product API Notes

## Goal

Build Phase 1 of the retail order service by adding product management with SQL persistence, validation, service-layer business rules, REST endpoints, tests, and documentation.

## What We Built

- `Product` JPA entity with SKU, name, description, price, quantity, active status, and timestamps.
- `ProductRepository` for Spring Data JPA persistence.
- Create, update, and response DTOs.
- `ProductService` for SKU uniqueness, validation, lookup, update, and soft delete rules.
- `ProductController` with `/products` endpoints.
- H2 console support for local database inspection.
- Unit tests and a Spring Boot API integration test suite.

## Why It Matters

Products are the foundation of the later order workflow. Orders will eventually reference products, check inventory, snapshot prices, and reject inactive products, so this phase establishes the first real business entity.

## Code Concepts

- JPA maps Java objects to SQL tables.
- DTOs keep API input and output separate from database entities.
- Bean Validation rejects bad HTTP payloads before business logic runs.
- The service layer owns retail rules such as unique SKUs and soft deletes.
- Soft delete keeps historical product records available for later order history.

## Files Changed

- `src/main/java/com/example/retailorderservice/entity/Product.java`
- `src/main/java/com/example/retailorderservice/repository/ProductRepository.java`
- `src/main/java/com/example/retailorderservice/dto/request/CreateProductRequest.java`
- `src/main/java/com/example/retailorderservice/dto/request/UpdateProductRequest.java`
- `src/main/java/com/example/retailorderservice/dto/response/ProductResponse.java`
- `src/main/java/com/example/retailorderservice/mapper/ProductMapper.java`
- `src/main/java/com/example/retailorderservice/service/ProductService.java`
- `src/main/java/com/example/retailorderservice/controller/ProductController.java`
- `src/main/java/com/example/retailorderservice/exception/DuplicateSkuException.java`
- `src/main/java/com/example/retailorderservice/exception/ProductNotFoundException.java`
- `src/main/resources/application.yml`
- `src/test/java/com/example/retailorderservice/service/ProductServiceTest.java`
- `src/test/java/com/example/retailorderservice/controller/ProductApiIntegrationTest.java`
- `README.md`
- `docs/api-examples.md`
- `docs/testing-guide.md`
- `docs/troubleshooting.md`
- `docs/architecture.md`
- `CHANGELOG.md`

## How to Run

```bash
mvn spring-boot:run
```

## Manual Demo Flow

These curl commands are for the video/demo walkthrough. Automated tests in `src/test/java/...` are the verification method for this phase.

Check health:

```bash
curl http://localhost:8080/actuator/health
```

Create a product:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "HAMMER-001",
    "name": "Steel Hammer",
    "description": "16 oz steel hammer",
    "price": 19.99,
    "quantityAvailable": 100,
    "active": true
  }'
```

List products:

```bash
curl http://localhost:8080/products
```

List active products with pagination:

```bash
curl "http://localhost:8080/products?active=true&page=0&size=20"
```

Find by SKU:

```bash
curl "http://localhost:8080/products?sku=HAMMER-001"
```

Get product by ID:

```bash
curl http://localhost:8080/products/1
```

Update a product:

```bash
curl -X PUT http://localhost:8080/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "HAMMER-001",
    "name": "Steel Hammer Pro",
    "description": "20 oz steel hammer",
    "price": 24.99,
    "quantityAvailable": 50,
    "active": true
  }'
```

Deactivate a product:

```bash
curl -X DELETE http://localhost:8080/products/1
```

Show duplicate SKU handling:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "HAMMER-001",
    "name": "Duplicate Hammer",
    "description": "Duplicate SKU demo",
    "price": 19.99,
    "quantityAvailable": 5,
    "active": true
  }'
```

Show validation:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "BAD-PRICE-001",
    "name": "Invalid Product",
    "description": "Zero price demo",
    "price": 0,
    "quantityAvailable": 5,
    "active": true
  }'
```

## How to Test

```bash
mvn test
```

The curl examples are for the video demo. The important product endpoint behavior is covered by `ProductApiIntegrationTest` so the phase can be verified automatically.

## Definition of Done

- `mvn test` passes.
- Automated tests cover the phase behavior.
- Manual curl demo commands are documented in this YouTube tutorial file.

## Common Errors

- `400 Bad Request`: missing SKU or name, price is zero or negative, or quantity is negative.
- `404 Not Found`: product ID does not exist.
- `409 Conflict`: SKU already exists.
- Empty product list after restart: H2 is running in memory for local development.

## Interview Talking Points

- Explain why the service layer owns business rules instead of the controller.
- Explain why DTOs reduce coupling between API contracts and database tables.
- Explain why SKU uniqueness matters in retail systems.
- Explain why soft delete is safer than physical delete for product history.
- Explain how unit and integration tests prove the phase is complete.

## Commit Message

```bash
git commit -m "Phase 1: implement product API with JPA persistence"
```
