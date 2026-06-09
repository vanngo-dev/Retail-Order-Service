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

## Phase 5

Run the full test suite:

```bash
mvn test
```

Run only the functional workflow tests:

```bash
mvn test -Dtest=OrderWorkflowIntegrationTest
```

Current integration and functional workflow coverage verifies:

- Product creation through the HTTP API.
- Order creation through the HTTP API.
- Inventory deduction after order creation.
- Product lookup after inventory changes.
- Order item snapshots for product ID, SKU, name, unit price, quantity, and line total.
- Order subtotal, fixed `8.25%` tax, and total during a full workflow.
- Shipment creation through the HTTP API.
- Order status transition from `CREATED` to `SHIPPED`.
- Shipment persistence for carrier and tracking number.
- Insufficient inventory workflows return `409` and do not deduct inventory, create orders, create order items, or create shipments.
- Duplicate shipment workflows return `409` and keep one shipment record.
- Invalid product payload workflows return `400` and do not persist products.

Manual curl examples are demo scripts in `docs/youtube/05-integration-functional-testing.md`. The important workflow behavior is automated in `OrderWorkflowIntegrationTest`.

## Phase 6

Run the full test suite:

```bash
mvn test
```

Run only the destructive and resilience tests:

```bash
mvn test -Dtest=DestructiveApiIntegrationTest
```

Current destructive and resilience coverage verifies:

- Malformed JSON returns `400` and does not persist products.
- Missing SKU, missing name, zero price, negative price, and negative quantity return `400`.
- Duplicate SKU returns `409` and keeps only the original product.
- Null customer email, bad email format, empty order items, and zero item quantity return `400`.
- Nonexistent product IDs return `404` and do not create orders or order items.
- Insufficient inventory returns `409` and does not deduct inventory.
- Inactive product order attempts return `409` and do not create orders or deduct inventory.
- Invalid path IDs return `400`.
- Missing shipment carrier and missing tracking number return `400` and keep the order in `CREATED`.
- Nonexistent order shipment attempts return `404` and do not create shipments.
- Duplicate shipment attempts return `409` and keep the original shipment.
- Cancelled order shipment attempts return `409` and do not create shipments.

Manual curl examples are demo scripts in `docs/youtube/06-destructive-testing.md`. The important destructive behavior is automated in `DestructiveApiIntegrationTest`.

## Phase 7

Run the full test suite:

```bash
mvn test
```

Current Docker and PostgreSQL profile checks verify:

- Default local tests still use H2.
- `mvn test` still passes with the PostgreSQL runtime dependency present.
- Docker Compose configuration defines the `retail-order-service` and `postgres` services.
- Docker Compose configuration starts the app with `SPRING_PROFILES_ACTIVE=docker`.
- The `docker` Spring profile points to PostgreSQL and disables the H2 console.

Manual Docker commands are demo and environment verification steps in `docs/youtube/07-docker-postgresql.md`. Automated tests remain the main verification for application behavior.
