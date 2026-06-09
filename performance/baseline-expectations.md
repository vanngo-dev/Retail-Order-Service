# Performance Baseline Expectations

Phase 11 adds basic k6 performance exposure. These are local-development smoke expectations, not production service-level objectives.

## Environment

- App: `retail-order-service`
- Runtime: local Spring Boot app
- Database: local H2 by default, or PostgreSQL when running through Docker Compose
- Authentication: HTTP Basic demo users from Phase 10
- Tool: k6

## Scripts

| Script | Purpose |
|---|---|
| `performance/k6/product-list.js` | Repeated `GET /products` requests |
| `performance/k6/order-workflow.js` | Product create, order create, order read, shipment workflow |

## Baseline Targets

| Scenario | Request volume | Average response time | p95 response time | Error rate |
|---|---:|---:|---:|---:|
| Product list smoke | 5 virtual users for 30 seconds | under 500 ms | under 1000 ms | under 1% |
| Order workflow smoke | 3 virtual users for 30 seconds | under 750 ms | under 1500 ms | under 5% |

## Notes

- Run performance scripts against a local environment that is already started.
- Run scripts one at a time so results are easy to interpret.
- Local laptop results can vary because CPU, disk, Docker, background apps, and JVM warmup all affect timings.
- Performance scripts are separate from Maven tests and do not replace automated Java tests.
- Manual run commands live in `docs/youtube/11-performance-testing.md`.
