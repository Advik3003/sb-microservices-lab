# Validation

## Why Validation Matters

Validation protects the service from bad input before the request reaches business logic or the database. Without it, every service method would need repetitive checks for missing names, invalid email addresses, negative quantities, and invalid prices.

In microservices, validation also improves API contracts. Other services and frontend clients can quickly learn what data is accepted and what errors are returned.

## How It Is Implemented

Validation is placed on request DTOs with Jakarta Validation annotations:

- `@NotBlank` for required strings.
- `@Email` for email format.
- `@Size` for maximum text length.
- `@Min` for minimum integer values.
- `@DecimalMin` for minimum decimal values.

Controllers use `@Valid @RequestBody`. Spring checks the DTO before entering the controller method body. If validation fails, Spring throws `MethodArgumentNotValidException`.

The global exception handler catches that exception and returns a structured error response with a `validationErrors` map. That keeps validation responses consistent across endpoints.

## How To Try It

Send an invalid user request:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"\",\"email\":\"wrong-email\",\"phone\":\"1\"}"
```

You should receive HTTP `400 Bad Request` with field-level validation messages.
