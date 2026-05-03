# Microservices Overview

This lab splits a small system into independently runnable services:

- `user-service` owns user data.
- `order-service` owns order data.
- `registry-server` lets services discover each other by service name.
- `config-server` serves shared configuration from `config-repo`.

The goal is to learn service boundaries, independent databases, service discovery, externalized config, and inter-service calls without adding complex business rules.
