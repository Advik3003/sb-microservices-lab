# Global Exception Handling

## Why Global Exception Handling

APIs should return predictable errors. If every controller writes its own `try/catch`, error formats become inconsistent and hard to maintain.

Global exception handling lets controllers stay focused on successful request handling while one central class converts exceptions into API responses.

## How It Is Implemented

Each business service has a `@RestControllerAdvice` class named `GlobalExceptionHandler`.

It handles:

- `ResourceNotFoundException` as HTTP `404`.
- duplicate or data conflict cases as HTTP `409`.
- validation failures as HTTP `400`.
- downstream service problems in `order-service` as HTTP `502`.
- unexpected errors as HTTP `500`.

The response body uses `ApiError`, which includes:

- timestamp
- status
- error
- message
- path
- validation errors

## How To Try It

Request a missing user:

```bash
curl http://localhost:8080/api/users/999
```

You should receive a structured HTTP `404` response instead of a raw stack trace. In production systems, this pattern is also where you would attach trace ids or support codes.
