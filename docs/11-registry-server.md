# Registry Server

`registry-server` runs Eureka on port `8761`.

Services register themselves with Eureka, and clients call other services by logical name instead of hard-coded host and port.

Examples:

- Feign uses `user-service`.
- RestTemplate uses `http://user-service/...`.

This introduces service discovery, which is useful when service instances move, scale, or run in containers.
