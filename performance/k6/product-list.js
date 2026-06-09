import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

export const options = {
  scenarios: {
    product_list_smoke: {
      executor: "constant-vus",
      vus: 5,
      duration: "30s",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["avg<500", "p(95)<1000"],
  },
};

export default function () {
  const response = http.get(`${BASE_URL}/products?active=true&page=0&size=20`);

  check(response, {
    "GET /products returns 200": (res) => res.status === 200,
    "GET /products returns a page": (res) => Boolean(res.json("content")),
  });

  sleep(1);
}
