# Swagger UI

## Why Swagger UI

Swagger UI gives you interactive API documentation. It is useful in a learning project because you can see request bodies, response models, validation constraints, and available endpoints without manually reading every controller.

In real teams, OpenAPI docs also help frontend developers, QA engineers, and other services understand an API contract.

## How It Is Implemented

`user-service` and `order-service` include `springdoc-openapi-starter-webmvc-ui`.

After running the services, open:

- `http://localhost:8081/swagger-ui/index.html` for users.
- `http://localhost:8082/swagger-ui/index.html` for orders.

The API Gateway also routes raw OpenAPI JSON:

- `http://localhost:8080/user-service/v3/api-docs`
- `http://localhost:8080/order-service/v3/api-docs`

This is the first step toward aggregated API documentation. A production gateway can add a custom Swagger UI that lists all downstream service docs in one place.

## How To Use It

Open Swagger UI, choose an endpoint, click "Try it out", enter JSON, and execute. Compare the generated request with the controller and DTO classes to understand how Spring maps HTTP to Java.
