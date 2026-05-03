# API Gateway

## Why API Gateway

An API Gateway gives clients one entry point into the system. Without a gateway, clients must know every service host and port, such as `user-service` on `8081` and `order-service` on `8082`.

That creates problems:

- Client apps become tightly coupled to internal service locations.
- Cross-cutting concerns like authentication, rate limiting, CORS, and request logging get duplicated.
- It becomes harder to change internal service names, ports, and deployment topology.

The gateway hides internal service layout and forwards requests to the correct service.

## How It Is Implemented

This project uses Spring Cloud Gateway in the `api-gateway` module.

The gateway runs on port `8080` and registers with Eureka as `api-gateway`.

Routes are configured in `api-gateway/src/main/resources/application.yml`:

- `/api/users/**` routes to `lb://user-service`.
- `/api/orders/**` routes to `lb://order-service`.

The `lb://` prefix means Spring Cloud LoadBalancer should resolve the service name through Eureka.

## How To Run It

Start services in this order:

```bash
mvn -pl registry-server spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl config-server spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl order-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

Then call:

```bash
curl http://localhost:8080/api/users
curl http://localhost:8080/api/orders
```

## What To Add Next

This gateway currently demonstrates routing only. Real gateways commonly add:

- authentication and authorization
- CORS handling
- rate limiting
- request and response logging
- correlation ids
- API version routing
- fallback responses
- OpenAPI aggregation

Keep those concerns in the gateway when they are truly cross-cutting. Business rules should still live in the owning service.
