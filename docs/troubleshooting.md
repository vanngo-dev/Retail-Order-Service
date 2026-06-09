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
