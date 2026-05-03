# Local and Centralised Logs

## Why Logs Matter In Microservices

In a monolith, one request usually stays inside one process. In microservices, one request can pass through the API Gateway, one business service, another business service, and a database. Logs help you understand what happened across that chain.

Local logs are useful while developing because they are simple files on your machine. Centralised logs are useful when there are many service instances and you need one place to search them.

## How Local Logs Are Implemented

`api-gateway`, `user-service`, and `order-service` each have `logback-spring.xml`.

They write console logs and rolling file logs:

- `logs/api-gateway.log`
- `logs/user-service.log`
- `logs/order-service.log`

The file pattern includes the service name, so central log tools can filter by service.

Tracing is also connected to logs. After OpenTelemetry tracing is enabled, log lines include `traceId` and `spanId`, which helps connect a log line to a distributed trace in Zipkin.

## How Centralised Logs Are Implemented Locally

This project uses Promtail, Loki, and Grafana in `docker-compose.yml`.

The flow is:

1. Spring Boot writes log files into `./logs`.
2. Promtail reads `./logs/*.log`.
3. Promtail pushes log lines to Loki.
4. Grafana queries Loki.

Run the logging stack:

```bash
docker compose up -d loki promtail grafana
```

Open Grafana at `http://localhost:3000` with `admin/admin`, add Loki as a data source if needed, and query logs by labels.

## Local Versus Production

Local logging can use files because it is easy to understand. In production, services often run in containers and write logs to stdout. A collector such as Promtail, Fluent Bit, Filebeat, or an OpenTelemetry Collector ships those logs to a central backend.

The same idea stays the same: services emit logs, collectors gather logs, and a central tool stores and searches logs.
