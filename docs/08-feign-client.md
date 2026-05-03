# Feign Client

Feign is a declarative HTTP client.

In `order-service`, `UserClient` calls `user-service` by service name:

```java
@FeignClient(name = "user-service", path = "/api/users")
```

When creating an order, `order-service` uses Feign to verify that the referenced user exists. Eureka resolves `user-service` to a running instance.
