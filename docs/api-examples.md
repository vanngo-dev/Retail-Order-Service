# API Examples

These are reusable API request examples. Step-by-step manual demo flows belong in the matching YouTube tutorial file under `docs/youtube/`.

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

## Products

Create a product:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "HAMMER-001",
    "name": "Steel Hammer",
    "description": "16 oz steel hammer",
    "price": 19.99,
    "quantityAvailable": 100,
    "active": true
  }'
```

Expected response shape:

```json
{
  "id": 1,
  "sku": "HAMMER-001",
  "name": "Steel Hammer",
  "description": "16 oz steel hammer",
  "price": 19.99,
  "quantityAvailable": 100,
  "active": true,
  "createdAt": "2026-06-04T12:00:00Z",
  "updatedAt": "2026-06-04T12:00:00Z"
}
```

List products:

```bash
curl http://localhost:8080/products
```

List active products:

```bash
curl "http://localhost:8080/products?active=true&page=0&size=20"
```

Find by SKU:

```bash
curl "http://localhost:8080/products?sku=HAMMER-001"
```

Get product by ID:

```bash
curl http://localhost:8080/products/1
```

Update a product:

```bash
curl -X PUT http://localhost:8080/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "HAMMER-001",
    "name": "Steel Hammer Pro",
    "description": "20 oz steel hammer",
    "price": 24.99,
    "quantityAvailable": 50,
    "active": true
  }'
```

Deactivate a product:

```bash
curl -X DELETE http://localhost:8080/products/1
```
