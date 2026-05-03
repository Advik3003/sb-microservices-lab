# Registry Server

## Why Service Discovery

In microservices, service instances may move, restart, or scale horizontally. Hard-coding `localhost:8081` inside another service works only for a small local demo.

Service discovery lets services register themselves under logical names. Other services call those names and let the discovery client find the actual instance.

## How It Is Implemented

`registry-server` runs Eureka on port `8761`.

The gateway and business services register as Eureka clients:

- `api-gateway`
- `user-service`
- `order-service`
- `config-server`

Feign, `RestTemplate`, and Gateway routes use logical service names:

- `lb://user-service`
- `lb://order-service`
- `http://user-service/api/users/{id}`

The `lb` prefix means "load-balanced". Spring Cloud uses Eureka registration data to choose an instance.

## How To Try It

Start `registry-server`, then start the other services. Open:

```text
http://localhost:8761
```

You should see registered applications. If a service is missing, check that it started successfully and that its `eureka.client.service-url.defaultZone` points to the registry.

## Production Notes

Kubernetes-based systems often use Kubernetes Services instead of Eureka. Eureka is still useful for learning and for Spring Cloud environments outside Kubernetes.
