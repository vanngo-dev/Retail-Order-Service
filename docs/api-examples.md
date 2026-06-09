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

## Orders

Create an order:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

Expected response shape:

```json
{
  "id": 1,
  "orderNumber": "ORD-1234ABCD",
  "customerEmail": "customer@example.com",
  "status": "CREATED",
  "subtotal": 39.98,
  "tax": 3.30,
  "total": 43.28,
  "createdAt": "2026-06-04T12:00:00Z",
  "updatedAt": "2026-06-04T12:00:00Z",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "skuSnapshot": "HAMMER-001",
      "productNameSnapshot": "Steel Hammer",
      "unitPriceSnapshot": 19.99,
      "quantity": 2,
      "lineTotal": 39.98
    }
  ]
}
```

List orders:

```bash
curl http://localhost:8080/orders
```

List created orders for a customer:

```bash
curl "http://localhost:8080/orders?status=CREATED&customerEmail=customer@example.com&page=0&size=20"
```

Get order by ID:

```bash
curl http://localhost:8080/orders/1
```

Ship an order:

```bash
curl -X POST http://localhost:8080/orders/1/ship \
  -H "Content-Type: application/json" \
  -d '{
    "carrier": "UPS",
    "trackingNumber": "1Z999999999"
  }'
```

Expected response shape:

```json
{
  "id": 1,
  "orderId": 1,
  "carrier": "UPS",
  "trackingNumber": "1Z999999999",
  "shippedAt": "2026-06-04T12:00:00Z",
  "createdAt": "2026-06-04T12:00:00Z"
}
```

## Error Responses

Standard error shape:

```json
{
  "timestamp": "2026-06-04T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Name is required",
  "path": "/products"
}
```

Validation error shape:

```json
{
  "timestamp": "2026-06-04T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Name is required",
  "path": "/products",
  "validationErrors": {
    "name": "Name is required"
  }
}
```

Duplicate SKU example:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "HAMMER-001",
    "name": "Duplicate Hammer",
    "description": "Duplicate SKU example",
    "price": 21.99,
    "quantityAvailable": 5,
    "active": true
  }'
```

Malformed JSON example:

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"sku":'
```
