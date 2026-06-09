# Testing Guide

## Testing Documentation Rule

All important endpoint behavior must be covered by automated tests in `src/test/java/...`.

Manual curl commands are only for demo/documentation purposes. Put step-by-step manual demo flows in the matching `docs/youtube/<phase-file>.md` file. The README may summarize endpoints, and `docs/api-examples.md` may keep reusable request examples.

Definition of Done:

- `mvn test` passes.
- Automated tests cover the phase behavior.
- Manual curl demo commands are documented in the YouTube tutorial file.

## Phase 0

Run the test suite:

```bash
mvn test
```

Current test coverage verifies:

- Spring application context starts.
- `GET /health` returns service status metadata.
- `GET /actuator/health` returns `UP`.

## Phase 1

Run the test suite:

```bash
mvn test
```

Current product coverage verifies:

- `ProductService` creates valid products.
- Duplicate SKUs are rejected.
- Negative quantity is rejected.
- Zero price is rejected.
- Product lookup by ID works.
- Product deactivation performs a soft delete.
- `POST /products` creates a product through the HTTP API.
- `GET /products` lists products with filters.
- `GET /products?sku=...&page=...&size=...` covers SKU filtering and pagination.
- `GET /products/{id}` returns a product by ID.
- `PUT /products/{id}` updates a product.
- `DELETE /products/{id}` deactivates a product.
- Missing name, zero price, and negative quantity payloads return `400`.
- Duplicate SKU create and update requests return `409`.
- Unknown product IDs return `404`.

Manual curl examples are demo scripts in `docs/youtube/01-product-api.md`. The important endpoint behavior is automated in `ProductApiIntegrationTest`.
