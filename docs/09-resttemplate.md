# RestTemplate

## Why RestTemplate

`RestTemplate` is the older, classic Spring HTTP client. Many existing Spring Boot applications still use it, so it is useful to understand even though new reactive applications often prefer `WebClient`.

This lab includes both Feign and `RestTemplate` so you can compare a declarative client with an imperative client.

## How It Is Implemented

`order-service` defines a `RestTemplate` bean with `@LoadBalanced`.

That allows this URL:

```text
http://user-service/api/users/{id}
```

The host name `user-service` is not a DNS name on your computer. Spring Cloud LoadBalancer and Eureka resolve it to a registered service instance.

`CustomerOrderService.update()` uses this `RestTemplate` path to verify the user before updating an order.

## How To Try It

Create a user and an order, then update the order through:

```bash
curl -X PUT http://localhost:8080/api/orders/1 \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productName\":\"Notebook\",\"quantity\":3,\"price\":199.00}"
```

The update path calls `user-service` through `RestTemplate`.

## When To Use It

Use `RestTemplate` when maintaining older synchronous Spring code. For new code, Feign is cleaner for service-to-service calls, and `WebClient` is better when you need non-blocking HTTP.
