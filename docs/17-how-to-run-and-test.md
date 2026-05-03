# How To Run And Test The Project

## Prerequisites

Install:

- Java 17
- Maven
- Docker Desktop
- curl, Postman, or any API client

Run all commands from the project root.

## Step 1: Start Infrastructure

Start PostgreSQL, logging, tracing, and optional local cloud services:

```bash
docker compose up -d postgres-user postgres-order loki promtail grafana zipkin localstack
```

Minimum required for CRUD:

```bash
docker compose up -d postgres-user postgres-order zipkin
```

Useful local URLs:

- Grafana: `http://localhost:3000`
- Loki: `http://localhost:3100`
- Zipkin: `http://localhost:9411`
- LocalStack: `http://localhost:4566`

## Step 2: Run Spring Boot Services

Run services in this order because later services depend on earlier services.

Terminal 1:

```bash
mvn -pl registry-server spring-boot:run -Dspring-boot.run.profiles=local
```

Terminal 2:

```bash
mvn -pl config-server spring-boot:run -Dspring-boot.run.profiles=local
```

Terminal 3:

```bash
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
```

Terminal 4:

```bash
mvn -pl order-service spring-boot:run -Dspring-boot.run.profiles=local
```

Terminal 5:

```bash
mvn -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

## Step 3: Check Service Health

Open Eureka:

```text
http://localhost:8761
```

You should see:

- `API-GATEWAY`
- `CONFIG-SERVER`
- `USER-SERVICE`
- `ORDER-SERVICE`

Check actuator health:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## Step 4: Test User CRUD Through Gateway

Create a user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"phone\":\"9876543210\"}"
```

List users:

```bash
curl http://localhost:8080/api/users
```

Get one user:

```bash
curl http://localhost:8080/api/users/1
```

Update a user:

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Alice Updated\",\"email\":\"alice.updated@example.com\",\"phone\":\"9876543210\"}"
```

Delete a user:

```bash
curl -X DELETE http://localhost:8080/api/users/1
```

## Step 5: Test Order CRUD Through Gateway

Create a user first, then create an order for that user.

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Bob\",\"email\":\"bob@example.com\",\"phone\":\"9876543211\"}"
```

Create an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productName\":\"Book\",\"quantity\":2,\"price\":499.00}"
```

List orders:

```bash
curl http://localhost:8080/api/orders
```

Filter orders by user:

```bash
curl "http://localhost:8080/api/orders?userId=1"
```

Update an order:

```bash
curl -X PUT http://localhost:8080/api/orders/1 \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productName\":\"Notebook\",\"quantity\":3,\"price\":199.00}"
```

Delete an order:

```bash
curl -X DELETE http://localhost:8080/api/orders/1
```

## Step 6: Test Validation And Errors

Send invalid user input:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"\",\"email\":\"bad-email\",\"phone\":\"1\"}"
```

Expected result: HTTP `400` with validation errors.

Request a missing user:

```bash
curl http://localhost:8080/api/users/999
```

Expected result: HTTP `404`.

Create an order with a missing user:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{\"userId\":999,\"productName\":\"Book\",\"quantity\":1,\"price\":100.00}"
```

Expected result: `order-service` calls `user-service`, the user lookup fails, and the API returns an error.

## Step 7: Test Swagger UI

Open:

- `http://localhost:8081/swagger-ui/index.html`
- `http://localhost:8082/swagger-ui/index.html`

Gateway-routed OpenAPI JSON:

- `http://localhost:8080/user-service/v3/api-docs`
- `http://localhost:8080/order-service/v3/api-docs`

## Step 8: Test Logs

Service logs are written to:

- `logs/api-gateway.log`
- `logs/user-service.log`
- `logs/order-service.log`

Start Grafana and Loki:

```bash
docker compose up -d loki promtail grafana
```

Open Grafana:

```text
http://localhost:3000
```

Use `admin/admin` locally. Add Loki as a data source if it is not already configured, then query service logs.

## Step 9: Test Zipkin Traces

Open Zipkin:

```text
http://localhost:9411
```

Send requests through the gateway, especially order create or update requests because they call `user-service`.

Then click "Run Query" in Zipkin. You should see traces that show gateway, order service, and user service spans.

## Step 10: Stop Everything

Stop Spring Boot services with `Ctrl+C` in each terminal.

Stop Docker containers:

```bash
docker compose down
```

To remove database and Grafana volumes:

```bash
docker compose down -v
```
