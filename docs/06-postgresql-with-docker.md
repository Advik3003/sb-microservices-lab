# PostgreSQL With Docker

`docker-compose.yml` starts one PostgreSQL database per business service:

- `postgres-user` on host port `5433`
- `postgres-order` on host port `5434`

This follows the microservices rule that each service owns its data. `user-service` should not directly query the order database, and `order-service` should not directly query the user database.

The `local` profile points each service to its own local Docker database.
