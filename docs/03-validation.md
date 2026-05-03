# Validation

Validation is implemented on request DTOs using Jakarta Validation annotations.

Examples:

- `@NotBlank` checks required strings.
- `@Email` checks email format.
- `@Min` checks numeric lower bounds.
- `@DecimalMin` checks money-like values.

Controllers use `@Valid @RequestBody` so invalid input is rejected before reaching the service layer. The global exception handler formats validation errors in a consistent response body.
