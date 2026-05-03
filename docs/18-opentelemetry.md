# OpenTelemetry

## Why OpenTelemetry

OpenTelemetry is the industry-standard way to collect telemetry from applications. Telemetry means traces, metrics, and logs. In this lab we use it for distributed tracing.

Without OpenTelemetry, each backend can pull your code toward its own SDK or exporter. Today you might send traces to Zipkin, tomorrow to Grafana Tempo, Jaeger, Datadog, New Relic, or a cloud provider. OpenTelemetry keeps the application instrumentation vendor-neutral.

The professional pattern is:

```text
Spring Boot service -> OTLP -> OpenTelemetry Collector -> observability backend
```

In this project, the backend is Zipkin because it is simple to run locally.

## How This Project Implements It

The traced services are:

- `api-gateway`
- `user-service`
- `order-service`

Each service uses Micrometer Tracing with the OpenTelemetry bridge:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```

Spring Boot creates spans for incoming HTTP requests and supported outgoing client calls. Micrometer bridges those spans to OpenTelemetry, and the OTLP exporter sends them to the collector.

Each service exports traces to the collector:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
```

For local learning, `probability: 1.0` traces every request. In production, reduce sampling to control cost and volume.

## OpenTelemetry Collector

The collector is configured in:

```text
docker/otel-collector/config.yml
```

It receives OTLP over both common protocols:

- gRPC on `4317`
- HTTP on `4318`

It batches trace data and exports it to Zipkin:

```text
Spring Boot services -> OTLP HTTP :4318 -> Collector -> Zipkin :9411
```

This is better than sending directly from every service to Zipkin because the collector becomes the routing and processing layer. Later, you can switch the exporter to Tempo, Jaeger, Datadog, or another backend without changing service code.

## How To Run It

Start Zipkin and the collector:

```bash
docker compose up -d zipkin otel-collector
```

For the full app, start the normal infrastructure:

```bash
docker compose up -d postgres-user postgres-order loki promtail grafana zipkin otel-collector
```

Then run the Spring Boot services:

```bash
mvn -pl registry-server spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl config-server spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl order-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

## How To Test It

Create a user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Trace User\",\"email\":\"trace.user@example.com\",\"phone\":\"9876543210\"}"
```

Create an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productName\":\"Tracing Book\",\"quantity\":1,\"price\":299.00}"
```

Open Zipkin:

```text
http://localhost:9411
```

Click "Run Query". You should see services such as:

- `api-gateway`
- `order-service`
- `user-service`

Open one trace. A good multi-service trace should show the gateway receiving the request, the order service handling it, and the user service being called during order creation.

## How To Check The Collector

Check that the collector container is running:

```bash
docker compose ps otel-collector
```

View collector logs:

```bash
docker compose logs otel-collector
```

If Zipkin has no traces:

- Confirm services are running with the `local` profile.
- Confirm `otel-collector` is running.
- Confirm `management.otlp.tracing.endpoint` is `http://localhost:4318/v1/traces`.
- Send a new request through the gateway after services have started.
- Check service logs for connection errors to port `4318`.

## Production Guidance

In production, keep application services exporting OTLP to a collector close to the workload. On Kubernetes, the collector often runs as a Deployment, DaemonSet, or sidecar depending on the platform.

Recommended practices:

- Use OTLP from applications, not backend-specific exporters.
- Put backend credentials only in the collector or platform secrets.
- Add resource attributes such as environment, service version, and region.
- Use sampling to control trace volume.
- Correlate traces with logs using `traceId` and `spanId`.
- Export to the backend your organization uses: Tempo, Jaeger, Zipkin, Elastic, Datadog, New Relic, CloudWatch, Azure Monitor, or Google Cloud Operations.

This lab keeps the collector simple, but the architecture is the same pattern used in production systems.
