# Microservices Overview

## Why Microservices

Microservices split one application into smaller services that can be developed, deployed, scaled, and owned independently. The main benefit is not "more projects"; it is clearer ownership. Each service should own a business capability and its data.

In this lab the business logic stays intentionally simple so the architecture is easy to see:

- `user-service` owns user CRUD.
- `order-service` owns order CRUD.
- `api-gateway` is the single client-facing entry point.
- `registry-server` lets services discover each other by name.
- `config-server` centralizes environment-specific configuration.

## How This Lab Implements It

The Maven parent project lists every service as a module. Each module is a Spring Boot application with its own port, configuration, dependencies, and startup command.

The services do not share one database. `user-service` uses `userdb`, and `order-service` uses `orderdb`. This demonstrates the important microservices rule that a service owns its data and other services should call its API instead of reading its tables.

`order-service` needs to check that a user exists before creating or updating an order. It does that through HTTP calls to `user-service`, once with Feign and once with `RestTemplate`, so you can compare both approaches.

## How To Run The Flow

Start infrastructure with Docker Compose, then run the services in this order: registry, config, user, order, gateway. After that, client calls should go through `api-gateway` on port `8080`.

The main test flow is:

1. Create a user with `POST http://localhost:8080/api/users`.
2. Create an order with `POST http://localhost:8080/api/orders` using that user's id.
3. List orders with `GET http://localhost:8080/api/orders`.

This keeps the learning path focused on service boundaries, discovery, configuration, logging, validation, and API routing.
