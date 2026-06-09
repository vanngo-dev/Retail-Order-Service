import http from "k6/http";
import encoding from "k6/encoding";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const ADMIN_USERNAME = __ENV.ADMIN_USERNAME || "admin";
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || "admin-password";
const USER_USERNAME = __ENV.USER_USERNAME || "user";
const USER_PASSWORD = __ENV.USER_PASSWORD || "user-password";

export const options = {
  scenarios: {
    order_workflow_smoke: {
      executor: "constant-vus",
      vus: 3,
      duration: "30s",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["avg<750", "p(95)<1500"],
  },
};

export default function () {
  const sku = `K6-${__VU}-${__ITER}-${Date.now()}`;
  const productId = createProduct(sku);

  if (!productId) {
    sleep(1);
    return;
  }

  const orderId = createOrder(productId);

  if (!orderId) {
    sleep(1);
    return;
  }

  getOrder(orderId);
  shipOrder(orderId);
  sleep(1);
}

function createProduct(sku) {
  const response = http.post(
    `${BASE_URL}/products`,
    JSON.stringify({
      sku,
      name: "k6 Demo Product",
      description: "Product created by k6 performance workflow",
      price: 19.99,
      quantityAvailable: 1000,
      active: true,
    }),
    {
      headers: jsonHeaders(ADMIN_USERNAME, ADMIN_PASSWORD),
      tags: { endpoint: "POST /products" },
    }
  );

  check(response, {
    "POST /products returns 201": (res) => res.status === 201,
    "POST /products returns an id": (res) => Boolean(res.json("id")),
  });

  return response.status === 201 ? response.json("id") : null;
}

function createOrder(productId) {
  const response = http.post(
    `${BASE_URL}/orders`,
    JSON.stringify({
      customerEmail: "performance-demo@example.com",
      items: [
        {
          productId,
          quantity: 1,
        },
      ],
    }),
    {
      headers: jsonHeaders(USER_USERNAME, USER_PASSWORD),
      tags: { endpoint: "POST /orders" },
    }
  );

  check(response, {
    "POST /orders returns 201": (res) => res.status === 201,
    "POST /orders returns an id": (res) => Boolean(res.json("id")),
  });

  return response.status === 201 ? response.json("id") : null;
}

function getOrder(orderId) {
  const response = http.get(`${BASE_URL}/orders/${orderId}`, {
    tags: { endpoint: "GET /orders/{id}" },
  });

  check(response, {
    "GET /orders/{id} returns 200": (res) => res.status === 200,
    "GET /orders/{id} returns CREATED or SHIPPED": (res) => ["CREATED", "SHIPPED"].includes(res.json("status")),
  });
}

function shipOrder(orderId) {
  const response = http.post(
    `${BASE_URL}/orders/${orderId}/ship`,
    JSON.stringify({
      carrier: "UPS",
      trackingNumber: `K6-${orderId}-${Date.now()}`,
    }),
    {
      headers: jsonHeaders(ADMIN_USERNAME, ADMIN_PASSWORD),
      tags: { endpoint: "POST /orders/{id}/ship" },
    }
  );

  check(response, {
    "POST /orders/{id}/ship returns 201": (res) => res.status === 201,
    "POST /orders/{id}/ship returns order id": (res) => res.json("orderId") === orderId,
  });
}

function jsonHeaders(username, password) {
  return {
    Authorization: basicAuth(username, password),
    "Content-Type": "application/json",
  };
}

function basicAuth(username, password) {
  return `Basic ${encoding.b64encode(`${username}:${password}`)}`;
}
