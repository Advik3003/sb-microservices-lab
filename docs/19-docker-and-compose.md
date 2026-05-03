# Docker And Docker Compose

## Why Docker

Docker packages an application with the runtime it needs. For this lab, Docker gives every Spring Boot service a repeatable Java 17 runtime and lets the full system run with one command.

Without Docker, you run infrastructure in Docker and Spring services with Maven. That is good for development. With the full Docker Compose file, you can run registry, config, gateway, user service, order service, databases, logs, tracing, and LocalStack together.

## Files Added

Each Spring Boot module has a Dockerfile:

- `registry-server/Dockerfile`
- `config-server/Dockerfile`
- `api-gateway/Dockerfile`
- `user-service/Dockerfile`
- `order-service/Dockerfile`

There are two Compose files:

- `docker-compose.yml`: infrastructure-only for Maven-based local development.
- `docker-compose.full.yml`: full-stack run, including all Spring Boot services.

## How To Run Everything In One Go

Build the Spring Boot JAR files first:

```bash
mvn clean package -DskipTests
```

Start the full stack:

```bash
docker compose -f docker-compose.full.yml up --build
```

Run in the background:

```bash
docker compose -f docker-compose.full.yml up --build -d
```

Stop everything:

```bash
docker compose -f docker-compose.full.yml down
```

Stop and remove volumes:

```bash
docker compose -f docker-compose.full.yml down -v
```

Use `down -v` only when you want to delete PostgreSQL and Grafana stored data.

## Dockerfile Explanation

All service Dockerfiles use the same pattern:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
```

This chooses the base image. `eclipse-temurin` is a trusted OpenJDK distribution. `17-jre` means the image contains the Java runtime, not the full JDK. `alpine` keeps the image small.

```dockerfile
WORKDIR /app
```

This sets `/app` as the working directory inside the container. Commands after this line run from `/app`.

```dockerfile
ARG JAR_FILE=target/*.jar
```

This defines a build-time variable. The Docker build expects the module JAR to already exist under `target/`.

```dockerfile
COPY ${JAR_FILE} app.jar
```

This copies the built Spring Boot JAR into the image and names it `app.jar`. Every service can then use the same startup command.

```dockerfile
EXPOSE 8080
```

This documents the port the container listens on. It does not publish the port by itself; Compose does that with `ports`.

```dockerfile
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

This is the container startup command. It runs the Spring Boot application.

## Important Compose Concepts

```yaml
services:
```

This is the root section where every container is declared.

```yaml
image: postgres:16-alpine
```

This tells Compose to use an existing image from Docker Hub.

```yaml
build:
  context: ./user-service
```

This tells Compose to build an image from the Dockerfile in that folder.

```yaml
container_name: ms-user-service
```

This gives the container a readable name. It is useful for logs and debugging.

```yaml
environment:
```

This passes environment variables into the container. Spring Boot converts environment variables like `SPRING_DATASOURCE_URL` into configuration properties like `spring.datasource.url`.

```yaml
ports:
  - "8080:8080"
```

This maps host port to container port. The left side is your machine, the right side is inside the container.

```yaml
volumes:
```

This mounts persistent data or local files into containers. PostgreSQL uses named volumes for durable database files. Services mount `./logs` so Promtail can collect local log files.

```yaml
depends_on:
  config-server:
    condition: service_healthy
```

This controls startup order. For example, `user-service` waits for `config-server` to be healthy before starting.

```yaml
healthcheck:
```

This tells Docker how to decide whether a container is healthy. Spring Boot services use the actuator health endpoint.

## Why Environment Overrides Are Needed In Containers

Inside a container, `localhost` means the same container, not your host machine and not another service.

That is why the full Compose file overrides these settings:

- Config Server URL becomes `http://config-server:8888`.
- Eureka URL becomes `http://registry-server:8761/eureka/`.
- User database URL becomes `jdbc:postgresql://postgres-user:5432/userdb`.
- Order database URL becomes `jdbc:postgresql://postgres-order:5432/orderdb`.
- OpenTelemetry endpoint becomes `http://otel-collector:4318/v1/traces`.

Compose service names act like DNS names on the Compose network.

## How To Test The Full Docker Stack

After starting `docker-compose.full.yml`, open:

- Eureka: `http://localhost:8761`
- Config Server: `http://localhost:8888/user-service/local`
- API Gateway: `http://localhost:8080`
- Users: `http://localhost:8080/api/users`
- Orders: `http://localhost:8080/api/orders`
- Zipkin: `http://localhost:9411`
- Grafana: `http://localhost:3000`

Create a user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Docker User\",\"email\":\"docker.user@example.com\",\"phone\":\"9876543210\"}"
```

Create an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productName\":\"Docker Book\",\"quantity\":1,\"price\":299.00}"
```

Check logs:

```bash
docker compose -f docker-compose.full.yml logs api-gateway
docker compose -f docker-compose.full.yml logs user-service
docker compose -f docker-compose.full.yml logs order-service
```

Check traces in Zipkin:

```text
http://localhost:9411
```

## Running Services As Separate Projects

In real microservices teams, each service often lives in its own Git repository. This lab is a Maven multi-module repository, but you can still run services separately.

### Option 1: Run A Single Module From This Repository

Start shared infrastructure:

```bash
docker compose up -d postgres-user postgres-order zipkin otel-collector
```

Run only the registry:

```bash
mvn -pl registry-server spring-boot:run -Dspring-boot.run.profiles=local
```

Run only the config server:

```bash
mvn -pl config-server spring-boot:run -Dspring-boot.run.profiles=local
```

Run one business service:

```bash
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
```

This is useful while developing one service.

### Option 2: Run Each Module As Its Own Folder

If you split the repository later, each service should keep:

- its own `pom.xml`
- its own `src/`
- its own `Dockerfile`
- its own README
- its own profile files

The service still needs external dependencies:

- `registry-server` for Eureka discovery
- `config-server` for centralized config
- PostgreSQL for data services
- OpenTelemetry Collector for traces

For example, if `user-service` becomes its own project, run shared infrastructure first, then run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

from inside the `user-service` project folder.

### Option 3: Run A Single Service Container

Build the JAR:

```bash
mvn -pl user-service clean package -DskipTests
```

Build the image:

```bash
docker build -t sb-microservices-lab/user-service:local ./user-service
```

Run it on the same Docker network as the dependencies:

```bash
docker compose -f docker-compose.full.yml up -d postgres-user registry-server config-server otel-collector
docker run --rm --name user-service-dev \
  --network sb-microservices-lab_default \
  -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-user:5432/userdb \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://registry-server:8761/eureka/ \
  -e MANAGEMENT_OTLP_TRACING_ENDPOINT=http://otel-collector:4318/v1/traces \
  sb-microservices-lab/user-service:local
```

On Windows PowerShell, use backticks instead of backslashes for multiline commands.

## Testing Separate Services

When testing a service separately, test in this order:

1. Health endpoint: `GET /actuator/health`.
2. Swagger UI or OpenAPI JSON.
3. CRUD endpoints directly on the service port.
4. Eureka registration in `http://localhost:8761`.
5. Gateway route if the gateway is running.
6. Zipkin trace after making a request.

For `user-service` direct testing:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8081/api/users
```

For `order-service`, make sure `user-service` is also running because order create and update validate the user through service-to-service calls.
