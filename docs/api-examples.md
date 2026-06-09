# API Examples

## Health

Custom service health:

```bash
curl http://localhost:8080/health
```

Expected shape:

```json
{
  "status": "UP",
  "service": "retail-order-service",
  "timestamp": "2026-06-04T12:00:00Z"
}
```

Actuator health:

```bash
curl http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```
