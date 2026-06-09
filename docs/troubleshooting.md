# Troubleshooting

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
