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

## Phase 2

Run the test suite:

```bash
mvn test
```

Current order coverage verifies:

- `OrderService` creates an order with one item.
- `OrderService` creates an order with multiple items.
- Missing customer email is rejected.
- Empty item lists are rejected.
- Nonexistent products are rejected.
- Inactive products are rejected.
- Insufficient inventory is rejected.
- Inventory is deducted when an order is created.
- Subtotal, fixed `8.25%` tax, total, and line totals are calculated.
- Product price, name, and SKU are snapshotted into order items.
- `POST /orders` creates orders through the HTTP API.
- `GET /orders/{id}` returns an order by ID.
- `GET /orders?status=...&customerEmail=...&page=...&size=...` covers filtering and pagination.
- Invalid order payloads return `400`.
- Nonexistent products and orders return `404`.
- Inactive products and insufficient inventory return `409`.

Manual curl examples are demo scripts in `docs/youtube/02-order-workflow.md`. The important endpoint behavior is automated in `OrderApiIntegrationTest`.

## Phase 3

Run the test suite:

```bash
mvn test
```

Current shipment coverage verifies:

- `OrderService` ships a valid `CREATED` order.
- A shipment record is created.
- Order status changes to `SHIPPED`.
- Nonexistent orders are rejected.
- Already shipped orders are rejected.
- Cancelled orders are rejected.
- Missing carrier is rejected.
- Missing tracking number is rejected.
- `POST /orders/{id}/ship` ships an order through the HTTP API.
- The shipped order can be read back with status `SHIPPED`.
- Duplicate shipment requests return `409`.
- Invalid shipment payloads return `400`.
- Unknown order IDs return `404`.

Manual curl examples are demo scripts in `docs/youtube/03-shipment-workflow.md`. The important endpoint behavior is automated in `ShipmentApiIntegrationTest`.

## Phase 4

Run the test suite:

```bash
mvn test
```

Current API quality coverage verifies:

- Duplicate SKU failures return the standard `409 Conflict` error response.
- Missing product name failures return the standard `400 Bad Request` validation response.
- Zero price failures return the standard `400 Bad Request` validation response.
- Unknown order IDs return the standard `404 Not Found` error response.
- Insufficient inventory failures return the standard `409 Conflict` error response.
- Duplicate shipment requests return the standard `409 Conflict` error response.
- Malformed JSON returns the standard `400 Bad Request` error response.
- Unexpected server errors are mapped to the standard `500 Internal Server Error` response shape.
- Validation failures include field-level `validationErrors` details.

Manual curl examples are demo scripts in `docs/youtube/04-error-handling-validation.md`. The important endpoint behavior is automated in `ApiErrorIntegrationTest` and `GlobalExceptionHandlerTest`.
