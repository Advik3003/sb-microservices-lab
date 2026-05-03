# PostgreSQL With Docker

## Why One Database Per Service

A core microservices rule is that each service owns its data. If `order-service` directly reads `user-service` tables, the services become tightly coupled and database changes become risky.

This lab gives each business service its own PostgreSQL database:

- `user-service` uses `userdb`.
- `order-service` uses `orderdb`.

## How It Is Implemented

`docker-compose.yml` starts two PostgreSQL containers:

- `postgres-user` on host port `5433`.
- `postgres-order` on host port `5434`.

The `local` profile in each service points to the correct database. Spring Data JPA repositories handle basic persistence, and Hibernate creates or updates tables locally with `ddl-auto: update`.

For production, the `prod` profile changes `ddl-auto` to `validate`. That is safer because production schema changes should normally be handled by a migration tool such as Flyway or Liquibase.

## How To Try It

Start the databases:

```bash
docker compose up -d postgres-user postgres-order
```

Then run `user-service` and `order-service`. Creating users and orders through the API will create rows in separate databases.

## What To Learn

`order-service` stores only `userId`; it does not store a full copy of user data. When it needs to check user existence, it calls `user-service`. That is the important boundary: data is private, APIs are public.
