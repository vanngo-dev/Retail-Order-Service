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

Confirm the app is running, then check the custom health endpoint at `/health` and the Actuator health endpoint at `/actuator/health`.

Manual diagnostic curl commands live in `docs/youtube/09-logging-monitoring-diagnostics.md`.

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

## Request Times Out or Test Appears to Hang

Check the basics first:

- Confirm the app is running before manual curl tests.
- Confirm the request URL points to `http://localhost:8080`.
- Confirm the JSON body is complete and the terminal command closed all quotes.
- Check the application logs for long-running requests or startup failures.
- Re-run `mvn test -Dtest=DestructiveApiIntegrationTest` to isolate destructive test failures from the full suite.

Phase 6 does not add performance or load testing. Timeout troubleshooting here is limited to local development and demo reliability.

## Docker Build Fails

Check that Docker is installed and running:

```bash
docker --version
docker compose version
```

Then retry:

```bash
docker build -t retail-order-service .
```

If the build fails while downloading base images or Maven dependencies, check network access and retry once the connection is available.

## Docker Compose App Cannot Connect to PostgreSQL

Start both services through Compose so the service names resolve correctly:

```bash
docker compose up --build
```

The app container should use:

```text
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/retail_order_service
```

Stop and remove the containers when finished:

```bash
docker compose down
```

If the database volume contains stale local data, remove it intentionally with:

```bash
docker compose down -v
```

Only use `-v` when you are comfortable deleting the local PostgreSQL volume.

## GitHub Actions CI Fails

Open the failed workflow run in GitHub Actions and check the first failed step.

Common causes:

- Java setup failed: confirm the workflow uses Java 21.
- Dependency download failed: rerun the workflow after network or Maven repository availability returns.
- Test step failed: run `mvn test` locally and fix the failing test or application behavior.
- Package step failed: run `mvn package` locally and confirm the Spring Boot jar is created under `target/`.
- CI badge does not show: confirm the repository path and workflow filename are correct.

The Phase 8 workflow only builds, tests, and packages the application. It does not deploy containers or run production infrastructure.

## Maven Package Fails Locally

If `mvn package` fails while writing under your local Maven cache, or reports missing classes from a Maven plugin, refresh the local dependency cache:

```bash
mvn package
```

If the user-level `.m2` directory is not writable, use a temporary Maven repository inside the project:

```bash
mvn -B -ntp "-Dmaven.repo.local=target/maven-repository" package
```

Common local causes:

- The terminal cannot write to `C:\Users\<you>\.m2\repository`.
- A plugin artifact is present but its POM or transitive dependencies are missing.
- Offline mode is enabled before the required package plugins are fully cached.

Fix the local Maven cache permissions or rerun with network access so Maven can download the missing plugin metadata. GitHub Actions uses a fresh hosted runner and Maven cache, so it can download these build plugins during CI.

## Actuator Info Endpoint Is Empty or Missing

Check `application.yml`:

- `/actuator/info` must be included in `management.endpoints.web.exposure.include`.
- `management.info.env.enabled` must be `true`.
- Service metadata should live under the `info.app` configuration key.

Phase 9 exposes only health and info diagnostics. Metrics remain out of scope for this phase.

## Expected Service Logs Do Not Appear

Check that the application log level includes the service package:

```yaml
logging:
  level:
    com.example.retailorderservice: INFO
```

Expected Phase 9 service logs include:

- `Product created`
- `Order created`
- `Inventory deducted`
- `Shipment created`
- `Insufficient inventory`
- `Invalid order state attempted`

Avoid adding customer email, full payloads, passwords, or shipment tracking numbers to routine logs.

## Request Returns 401 Unauthorized

The endpoint requires authentication and no valid credentials were provided. Phase 10 uses HTTP Basic with local demo users.

Demo credentials:

- `user` / `user-password`
- `admin` / `admin-password`

Manual security curl commands live in `docs/youtube/10-basic-security.md`.

## Request Returns 403 Forbidden

The credentials are valid, but the authenticated user does not have the required role.

Common examples:

- `USER` cannot create, update, or deactivate products.
- `USER` cannot ship orders.
- `ADMIN` can perform protected product and shipment operations.

This phase does not add JWT, OAuth, database-backed users, or frontend login.

## H2 Console Is Blocked or Blank

Phase 10 allows `/h2-console/**` for local development and sets same-origin frame options. If the console still does not load, confirm the app is running in local H2 mode and open:

```text
http://localhost:8080/h2-console
```

The H2 console is a local development tool, not a production feature.

## k6 Command Is Not Available

Install k6 and reopen the terminal so the command is available on PATH.

Manual performance commands live in `docs/youtube/11-performance-testing.md`.

## k6 Script Cannot Connect to the App

Confirm the Spring Boot app is running and `BASE_URL` points to the correct host and port.

Useful checks:

- `GET /actuator/health` should return `UP`.
- Local mode normally uses `http://localhost:8080`.
- Docker Compose mode also maps the app to `http://localhost:8080` by default.

## k6 Script Gets 401 or 403

Phase 11 scripts call protected write endpoints from Phase 10.

Check credentials and roles:

- Product creation requires `admin` / `admin-password`.
- Order creation can use `user` / `user-password`.
- Shipment creation requires `admin` / `admin-password`.

## k6 Results Look Slow Locally

Local performance numbers are sensitive to laptop load, Docker, JVM warmup, antivirus scans, and database mode.

Try:

- Run one k6 script at a time.
- Warm up the app with a few requests before recording results.
- Compare average response time, p95 response time, request count, and error rate instead of relying on one request.
- Treat local results as smoke-test baselines, not production service-level objectives.

## Final Demo IDs Do Not Match The Script

The final demo script assumes a fresh local H2 run, where the first product is usually ID `1` and the first order is usually ID `1`.

If IDs differ:

- Restart the app in local H2 mode to reset the in-memory database.
- Read the `Location` header from create responses and use that ID in later commands.
- In Docker Compose mode, remember PostgreSQL data persists in the Docker volume until you intentionally remove it.

Manual final demo commands live in `docs/youtube/12-final-demo-portfolio-polish.md`.

## Final Demo Write Request Returns 401 Or 403

The finished project protects write operations.

Use:

- `admin` / `admin-password` for product create, product update, product deactivate, and shipment creation.
- `user` / `user-password` for order creation.

Read endpoints, health endpoints, and diagnostic endpoints remain public for the demo.
