# Global Exception Handling

Each business service has a `@RestControllerAdvice` class.

The handler converts exceptions into consistent JSON responses with:

- timestamp
- HTTP status code
- error reason
- message
- request path
- validation errors

This avoids repeating `try/catch` blocks in every controller and keeps API errors predictable.
