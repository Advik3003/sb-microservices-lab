# Feign Client

## Why Feign

Feign is a declarative HTTP client. Instead of manually building URLs and parsing responses, you define a Java interface and Spring creates the HTTP client implementation.

Feign is useful when one service needs to call another service often and you want the call to look like a typed Java method.

## How It Is Implemented

`order-service` enables Feign with `@EnableFeignClients`.

The `UserClient` interface points to `user-service`:

```java
@FeignClient(name = "user-service", path = "/api/users")
```

The method `findById(Long id)` maps to `GET /api/users/{id}`. Because the client name is `user-service`, Eureka is used to find the actual running instance.

`CustomerOrderService.create()` calls Feign before saving an order. If the user does not exist, the create request fails instead of storing an invalid `userId`.

## How To Try It

Start `registry-server`, `user-service`, and `order-service`. Then create an order with a missing `userId`. Feign will call `user-service`, receive a not-found response, and `order-service` will convert that into an API error.

## When To Use Feign

Use Feign when you want clean service-to-service HTTP clients with typed interfaces. For more advanced production systems, combine it with timeouts, retries, circuit breakers, and tracing.
