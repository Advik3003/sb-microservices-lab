# CRUD Operations

## Why CRUD First

CRUD means create, read, update, and delete. It is the simplest useful API shape for learning microservices because it lets you focus on service structure instead of complex domain rules.

This project keeps CRUD small on purpose:

- `user-service` exposes `/api/users`.
- `order-service` exposes `/api/orders`.

## How It Is Implemented

Each CRUD service follows the same structure:

- Controller: receives HTTP requests and returns HTTP responses.
- Request DTO: describes and validates input.
- Response DTO: describes output without exposing the JPA entity directly.
- Service: holds business operations and transaction boundaries.
- Repository: uses Spring Data JPA for database access.
- Entity: maps Java objects to PostgreSQL tables.

For example, `UserController` accepts `UserRequest`, calls `UserAccountService`, and returns `UserResponse`. The service maps between DTOs and `UserAccount`, then saves through `UserAccountRepository`.

## How To Try It

Create a user through the gateway:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"phone\":\"9876543210\"}"
```

Create an order for that user:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productName\":\"Book\",\"quantity\":2,\"price\":499.00}"
```

Use direct service URLs only when you want to understand one service in isolation. For client-like testing, prefer the gateway URLs.
