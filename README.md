# Spring Boot Microservices Lab

A small learning project that demonstrates common microservices concepts with CRUD-only business logic.

## Modules

- `registry-server`: Eureka service registry.
- `config-server`: Spring Cloud Config Server backed by local files in `config-repo`.
- `api-gateway`: Spring Cloud Gateway entry point for client traffic.
- `user-service`: CRUD API for users, PostgreSQL, validation, global exceptions, Swagger UI.
- `order-service`: CRUD API for orders, PostgreSQL, validation, global exceptions, Swagger UI, Feign, and RestTemplate calls to `user-service`.

## Run Locally

Start infrastructure:

```bash
docker compose up -d postgres-user postgres-order loki promtail grafana zipkin otel-collector
```

Run services in this order:

```bash
mvn -pl registry-server spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl config-server spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl order-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

## URLs

- Eureka: http://localhost:8761
- Config Server: http://localhost:8888/user-service/local
- API Gateway: http://localhost:8080
- Users through Gateway: http://localhost:8080/api/users
- Orders through Gateway: http://localhost:8080/api/orders
- User Swagger UI: http://localhost:8081/swagger-ui/index.html
- Order Swagger UI: http://localhost:8082/swagger-ui/index.html
- Grafana: http://localhost:3000
- OpenTelemetry Collector OTLP HTTP: http://localhost:4318
- Zipkin: http://localhost:9411

## Profiles

Every service includes `local`, `dev`, `prod`, and `localstack` profiles. The `local` profile is intended for Docker Compose on a developer machine.

## Documentation

Concept tutorials live in `docs/`. Start with `docs/17-how-to-run-and-test.md` for the full local runbook.
