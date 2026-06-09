# Troubleshooting

## API Error Response Shape

API failures return a consistent JSON response:

```json
{
  "timestamp": "2026-06-04T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Name is required",
  "path": "/products"
}
```

Validation failures may also include `validationErrors` with field-specific messages.

## Java or Maven Is Not Available

If `java -version` or `mvn -version` fails, install Java 21 and Maven, then reopen the terminal so PATH is refreshed.

Useful checks:

```bash
java -version
mvn -version
```

## Health Endpoint Does Not Respond

Start the app:

```bash
mvn spring-boot:run
```

Then verify:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/health
```

## H2 Console Cannot Connect

Start the app and open:

```text
http://localhost:8080/h2-console
```

Use:

```text
JDBC URL: jdbc:h2:mem:retail_order_service
User: sa
Password:
```

The password is blank. Because this is an in-memory database, data is reset when the app restarts.

## Product Create Returns 409 Conflict

SKU values must be unique. Use `GET /products?sku=YOUR-SKU` to check whether a SKU already exists before retrying.

## Product Create or Update Returns 400 Bad Request

Check the request body:

- `sku` is required.
- `name` is required.
- `price` must be greater than zero.
- `quantityAvailable` cannot be negative.

## Git Reports Dubious Ownership

On some Windows file systems, Git may refuse commands with a `dubious ownership` warning. Run the command Git suggests for this repository if you trust the folder:

```bash
git config --global --add safe.directory D:/MyApps/Java/RetailOrderService
```

## Order Create Returns 400 Bad Request

Check the request body:

- `customerEmail` is required and must be a valid email address.
- `items` must contain at least one order item.
- Each item must include `productId`.
- Each item must have `quantity` greater than zero.

## Order Create Returns 404 Not Found

At least one requested `productId` does not exist. Create the product first, then retry the order.

## Order Create Returns 409 Conflict

The product exists, but it cannot be ordered in its current state. Common causes:

- The product is inactive.
- The requested quantity is greater than available inventory.

## Order Totals Look Slightly Rounded

Phase 2 uses a simplified fixed `8.25%` tax rate for portfolio purposes. Monetary values are rounded to two decimals with standard half-up rounding.

## Ship Order Returns 400 Bad Request

Check the request body:

- `carrier` is required.
- `trackingNumber` is required.

## Ship Order Returns 404 Not Found

The order ID does not exist. Create an order first, then retry the shipment request.

## Ship Order Returns 409 Conflict

The order exists, but it cannot be shipped in its current state. Common causes:

- The order has already been shipped.
- The order is cancelled.
- The order is not in `CREATED` status.

## Request Returns Malformed JSON

Check that the request body is valid JSON:

- Property names and string values must use double quotes.
- Objects and arrays must have matching braces and brackets.
- The `Content-Type` header should be `application/json`.

## Request Returns 500 Internal Server Error

The API hides unexpected server details behind a generic message. Check the application logs for the stack trace, fix the underlying issue, and add a regression test if the failure represents a missing business or validation rule.
