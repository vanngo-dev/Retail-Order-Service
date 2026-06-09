# Docker and PostgreSQL Profile Notes

## Goal

Build Phase 7 of the retail order service by adding Docker support and a PostgreSQL-backed Docker Compose setup while keeping H2 as the default local development database.

## What We Built

- `Dockerfile`.
- `.dockerignore`.
- `docker-compose.yml`.
- PostgreSQL service for Docker Compose.
- Spring `docker` profile in `application-docker.yml`.
- PostgreSQL JDBC runtime dependency.
- Documentation for local H2 mode and Docker PostgreSQL mode.

## Why It Matters

Docker makes the runtime environment repeatable. H2 is fast and convenient for local development and automated tests, while PostgreSQL is closer to a production relational database. Docker Compose lets the app and database start together with predictable configuration.

## Code Concepts

- Local development uses `application.yml` and H2 by default.
- Docker Compose sets `SPRING_PROFILES_ACTIVE=docker`.
- The `docker` profile uses PostgreSQL and disables the H2 console.
- The app service depends on a healthy PostgreSQL service.
- `mvn test` still proves application behavior without requiring Docker.

## Files Changed

- `pom.xml`
- `Dockerfile`
- `.dockerignore`
- `docker-compose.yml`
- `src/main/resources/application-docker.yml`
- `README.md`
- `docs/testing-guide.md`
- `docs/architecture.md`
- `docs/troubleshooting.md`
- `docs/youtube/07-docker-postgresql.md`
- `CHANGELOG.md`

## Local H2 Mode

Run the app locally:

```bash
mvn spring-boot:run
```

Verify:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/products
```

Local H2 console:

```text
http://localhost:8080/h2-console
```

Use:

```text
JDBC URL: jdbc:h2:mem:retail_order_service
User: sa
Password:
```

## Docker PostgreSQL Mode

Build the Docker image:

```bash
docker build -t retail-order-service .
```

Start the app and PostgreSQL:

```bash
docker compose up --build
```

Verify health:

```bash
curl http://localhost:8080/actuator/health
```

Verify the Product API:

```bash
curl http://localhost:8080/products
```

Create a product in PostgreSQL-backed mode:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "HAMMER-001",
    "name": "Steel Hammer",
    "description": "16 oz steel hammer",
    "price": 19.99,
    "quantityAvailable": 10,
    "active": true
  }'
```

Stop containers:

```bash
docker compose down
```

Stop containers and delete the local PostgreSQL volume:

```bash
docker compose down -v
```

Only use `-v` when you intentionally want to delete local PostgreSQL data.

## How to Test

Run the automated test suite:

```bash
mvn test
```

The Docker commands are for environment verification and video/demo walkthroughs. The important application behavior remains covered by automated tests in `src/test/java/...`.

## Common Errors

- Docker command not found: install Docker Desktop or Docker Engine and reopen the terminal.
- Compose cannot connect to PostgreSQL: use `docker compose up --build` so app and database share the Compose network.
- Port already in use: stop the process using port `8080` or `5432`, or change the host port mapping in `docker-compose.yml`.
- Stale database data: run `docker compose down -v` only when deleting the local PostgreSQL volume is acceptable.

## Interview Talking Points

- Explain why Docker improves deployment consistency.
- Explain why H2 is useful locally but PostgreSQL is a better container database.
- Explain how Docker Compose wires the app and database together.
- Explain why profiles let the same codebase support multiple runtime environments.
- Explain how Docker support prepares the project for cloud and CI/CD workflows without implementing CI yet.

## Definition of Done

- `mvn test` passes.
- Local H2 remains the default configuration.
- Docker Compose uses PostgreSQL through the `docker` Spring profile.
- Manual Docker demo commands are documented in this YouTube tutorial file.

## Commit Message

```bash
git commit -m "Phase 7: add Docker and PostgreSQL compose setup"
```
