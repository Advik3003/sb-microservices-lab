# CRUD Operations

CRUD means create, read, update, and delete.

In this project:

- `user-service` exposes `/api/users`.
- `order-service` exposes `/api/orders`.

Each service has a controller, service class, repository, entity, request DTO, and response DTO. This keeps HTTP concerns, business operations, persistence, and API shapes separate enough for learning.
