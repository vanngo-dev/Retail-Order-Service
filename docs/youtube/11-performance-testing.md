# Performance Testing Notes

## Goal

Build Phase 11 of the retail order service by adding basic performance testing exposure with k6 scripts for key API workflows.

## What We Built

- `performance/k6/product-list.js`
- `performance/k6/order-workflow.js`
- `performance/baseline-expectations.md`
- README performance summary
- Testing, architecture, troubleshooting, changelog, and YouTube documentation updates

## Why It Matters

Functional tests prove that behavior is correct. Performance tests give a first look at how the API behaves under repeated traffic. For retail systems, endpoints like product listing, order creation, and shipment workflows matter because customers and operations teams depend on quick, reliable responses.

## Code Concepts

- k6 scripts are JavaScript files that call HTTP endpoints repeatedly.
- Virtual users simulate multiple clients.
- Average response time shows the typical request time.
- p95 response time shows the slower end of normal traffic.
- Error rate shows how often requests fail.
- Performance scripts are separate from Maven tests.

## Files Changed

- `performance/k6/product-list.js`
- `performance/k6/order-workflow.js`
- `performance/baseline-expectations.md`
- `README.md`
- `docs/testing-guide.md`
- `docs/architecture.md`
- `docs/troubleshooting.md`
- `docs/youtube/11-performance-testing.md`
- `CHANGELOG.md`

## Manual Performance Demo Flow

These commands are for the video/demo walkthrough. The Java test suite still verifies functional correctness.

Start the app locally:

```bash
mvn spring-boot:run
```

In a second terminal, confirm health:

```bash
curl http://localhost:8080/actuator/health
```

Run the product list smoke test:

```bash
k6 run performance/k6/product-list.js
```

Run the order workflow smoke test:

```bash
k6 run performance/k6/order-workflow.js
```

Run against a different base URL:

```bash
k6 run -e BASE_URL=http://localhost:8080 performance/k6/order-workflow.js
```

Run with explicit demo credentials:

```bash
k6 run \
  -e ADMIN_USERNAME=admin \
  -e ADMIN_PASSWORD=admin-password \
  -e USER_USERNAME=user \
  -e USER_PASSWORD=user-password \
  performance/k6/order-workflow.js
```

## Baseline Expectations

| Scenario | Request volume | Average response time | p95 response time | Error rate |
|---|---:|---:|---:|---:|
| Product list smoke | 5 virtual users for 30 seconds | under 500 ms | under 1000 ms | under 1% |
| Order workflow smoke | 3 virtual users for 30 seconds | under 750 ms | under 1500 ms | under 5% |

## How to Test the Java App

Run the automated Java test suite:

```bash
mvn test
```

Run only the security tests if credentials or roles are in question:

```bash
mvn test -Dtest=SecurityIntegrationTest
```

## Common Errors

- `k6` command not found: install k6 and reopen the terminal.
- Connection refused: start the Spring Boot app first.
- `401 Unauthorized`: confirm the demo username and password.
- `403 Forbidden`: confirm the script uses `admin` for product creation and shipping.
- High response times on first run: rerun after JVM warmup.
- Duplicate SKU conflicts: the workflow script generates unique SKU values per iteration.

## Interview Talking Points

- Explain the difference between correctness testing and performance testing.
- Explain why product listing and order workflows are important retail paths.
- Explain average response time and p95 response time.
- Explain why local performance tests are useful but not production guarantees.
- Explain why performance scripts should not replace unit and integration tests.

## Definition of Done

- `mvn test` passes.
- k6 scripts exist under `performance/k6`.
- Scripts cover `GET /products`, `POST /products`, `POST /orders`, `GET /orders/{id}`, and `POST /orders/{id}/ship`.
- Baseline expectations are documented.
- Manual performance commands are documented in this YouTube tutorial file.

## Commit Message

```bash
git commit -m "Phase 11: add basic performance testing scripts"
```
