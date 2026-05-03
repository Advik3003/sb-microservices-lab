# OpenTelemetry

## Why OpenTelemetry

OpenTelemetry is the industry-standard way to collect telemetry from applications. Telemetry means traces, metrics, and logs. In this lab we use it for distributed tracing.

Without OpenTelemetry, each backend can pull your code toward its own SDK or exporter. Today you might send traces to Zipkin, tomorrow to Grafana Tempo, Jaeger, Datadog, New Relic, or a cloud provider. OpenTelemetry keeps the application instrumentation vendor-neutral.

The professional pattern is:

```text
Spring Boot service -> OTLP -> OpenTelemetry Collector -> observability backend
```

In this project, the backend is Zipkin because it is simple to run locally.

## Final Architecture

This is the tracing path used by the project:

```text
Client
  -> api-gateway
  -> user-service / order-service
  -> OpenTelemetry OTLP exporter
  -> OpenTelemetry Collector
  -> Zipkin
```

All five Spring Boot services are traced:

- `registry-server`
- `config-server`
- `api-gateway`
- `user-service`
- `order-service`

The registry and config server are included because they are part of the platform. In production, tracing infrastructure services is useful when startup, config loading, discovery, or routing behaves unexpectedly.

## Implementation Step 1: Add Dependencies

Each service uses Micrometer Tracing with the OpenTelemetry bridge.

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

Why these dependencies:

- `micrometer-tracing-bridge-otel`: connects Spring Boot Micrometer tracing to OpenTelemetry.
- `opentelemetry-exporter-otlp`: sends traces from the app to an OTLP endpoint.

We use OTLP instead of a Zipkin-specific application exporter because OTLP keeps application code vendor-neutral. The collector decides where traces go.

## Implementation Step 2: Configure Tracing In Base Config

Each service has tracing in its base `application.yml`:

```yaml
management:
  tracing:
    sampling:
      probability: ${MANAGEMENT_TRACING_SAMPLING_PROBABILITY:1.0}
  otlp:
    tracing:
      endpoint: ${MANAGEMENT_OTLP_TRACING_ENDPOINT:http://localhost:4318/v1/traces}
```

Why this is in base config:

- `application.yml` is loaded for every profile.
- `application-local.yml`, `application-dev.yml`, `application-prod.yml`, and `application-localstack.yml` inherit it.
- That means OpenTelemetry is enabled for all profiles unless a profile explicitly overrides it.

Why environment variables are used:

- Local Maven runs can use `http://localhost:4318/v1/traces`.
- Docker Compose runs can override to `http://otel-collector:4318/v1/traces`.
- Dev/prod can point to a collector URL from that environment.
- Sampling can be reduced in production without changing code.

Example production-style overrides:

```bash
MANAGEMENT_OTLP_TRACING_ENDPOINT=http://otel-collector.observability:4318/v1/traces
MANAGEMENT_TRACING_SAMPLING_PROBABILITY=0.1
```

For local learning, `probability: 1.0` traces every request. In production, reduce sampling to control cost and volume.

## Implementation Step 3: Add The Collector

The collector is configured in:

```text
docker/otel-collector/config.yml
```

It receives OTLP over both common protocols:

- gRPC on `4317`
- HTTP on `4318`

The collector config is:

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch:

exporters:
  zipkin:
    endpoint: http://zipkin:9411/api/v2/spans
  debug:
    verbosity: basic

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [zipkin, debug]
```

What each part means:

- `receivers.otlp`: accepts telemetry from services.
- `grpc.endpoint`: accepts OTLP/gRPC on `4317`.
- `http.endpoint`: accepts OTLP/HTTP on `4318`, which Spring Boot uses here.
- `processors.batch`: groups spans before export for better efficiency.
- `exporters.zipkin`: forwards traces to Zipkin for visualization.
- `exporters.debug`: prints trace export activity in collector logs, useful for learning and troubleshooting.
- `service.pipelines.traces`: connects receiver, processor, and exporters into one trace pipeline.

The local flow is:

```text
Spring Boot services -> OTLP HTTP :4318 -> Collector -> Zipkin :9411
```

This is better than sending directly from every service to Zipkin because the collector becomes the routing and processing layer. Later, you can switch the exporter to Tempo, Jaeger, Datadog, or another backend without changing service code.

## Implementation Step 4: Add Docker Compose

The infrastructure Compose file starts the collector:

```yaml
otel-collector:
  image: otel/opentelemetry-collector-contrib:0.111.0
  command: ["--config=/etc/otelcol-contrib/config.yml"]
  ports:
    - "4317:4317"
    - "4318:4318"
    - "8889:8889"
  volumes:
    - ./docker/otel-collector/config.yml:/etc/otelcol-contrib/config.yml:ro
  depends_on:
    - zipkin
```

Why these Compose lines matter:

- `image`: uses the OpenTelemetry Collector distribution with many exporters included.
- `command`: tells the collector which config file to load.
- `4317`: exposes OTLP/gRPC.
- `4318`: exposes OTLP/HTTP.
- `8889`: reserved for collector metrics/debug visibility.
- `volumes`: mounts our collector config into the container as read-only.
- `depends_on`: starts Zipkin before the collector tries to export spans to it.

The full-stack Compose file also overrides the app endpoint:

```yaml
MANAGEMENT_OTLP_TRACING_ENDPOINT: http://otel-collector:4318/v1/traces
```

Inside Docker, `localhost` means the current container, so services must use the Compose service name `otel-collector`.

## How To Run It With Maven Services

Start infrastructure:

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

## How To Run It With Full Docker Compose

Build JARs:

```bash
mvn clean package -DskipTests
```

Run everything:

```bash
docker compose -f docker-compose.full.yml up --build
```

In full Docker mode, the Compose environment variables point every service to the collector container.

## Testing Option 1: Smoke Test The Collector

Check that the collector is running:

```bash
docker compose ps otel-collector
```

Expected result: state should be `running`.

Check collector logs:

```bash
docker compose logs --tail=50 otel-collector
```

Healthy startup logs should mention:

- `Starting GRPC server`
- `Starting HTTP server`
- `Everything is ready`

After sending requests, logs should show `TracesExporter` entries with span counts.

## Testing Option 2: Check Health Endpoints

Call service health endpoints:

```bash
curl http://localhost:8761/actuator/health
curl http://localhost:8888/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

These calls also create traces for the services that receive them.

## Testing Option 3: Generate A Gateway Trace

Gateway-only request:

```bash
curl http://localhost:8080/api/users
```

This should create at least an `api-gateway` trace and a downstream `user-service` trace because the gateway routes to the user service.

Order list request:

```bash
curl http://localhost:8080/api/orders
```

This should create gateway and order service spans.

## Testing Option 4: Generate A Multi-Service Business Trace

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

This is the most useful test because order creation calls `user-service` with Feign to validate the user. In Zipkin, you should see a trace involving:

- `api-gateway`
- `order-service`
- `user-service`

Update the order to test the `RestTemplate` path:

```bash
curl -X PUT http://localhost:8080/api/orders/1 \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productName\":\"Tracing Notebook\",\"quantity\":2,\"price\":199.00}"
```

This validates the user through the load-balanced `RestTemplate` path.

## Testing Option 5: Verify In Zipkin

Open Zipkin:

```text
http://localhost:9411
```

Click "Run Query". You should see services such as:

- `registry-server`
- `config-server`
- `api-gateway`
- `order-service`
- `user-service`

Open one trace. A good multi-service trace should show:

- gateway receiving the request
- gateway forwarding to a business service
- order service handling create/update
- order service calling user service
- timing for each span

## Testing Option 6: Query Zipkin API

List services known to Zipkin:

```bash
curl http://localhost:9411/api/v2/services
```

Expected services:

- `api-gateway`
- `config-server`
- `order-service`
- `registry-server`
- `user-service`

Query traces for one service:

```bash
curl "http://localhost:9411/api/v2/traces?serviceName=api-gateway&limit=5"
```

If this returns JSON trace data, the app -> collector -> Zipkin path is working.

## Testing Option 7: Correlate Logs With Traces

The service logback patterns include:

```text
trace=%X{traceId:-none} span=%X{spanId:-none}
```

Make a request, then inspect logs:

```bash
docker compose logs --tail=100 api-gateway
```

Or when running with Maven, check local log files:

```text
api-gateway/logs/api-gateway.log
user-service/logs/user-service.log
order-service/logs/order-service.log
```

Find a `traceId` in the log and search the same trace in Zipkin. This is how traces and logs work together in real debugging.

## Testing Option 8: Test All Profiles

OpenTelemetry is in base `application.yml`, so every profile inherits it.

Run with local:

```bash
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
```

Run with dev:

```bash
set MANAGEMENT_OTLP_TRACING_ENDPOINT=http://localhost:4318/v1/traces
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=dev
```

Run with localstack:

```bash
set MANAGEMENT_OTLP_TRACING_ENDPOINT=http://localhost:4318/v1/traces
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=localstack
```

PowerShell syntax:

```powershell
$env:MANAGEMENT_OTLP_TRACING_ENDPOINT = "http://localhost:4318/v1/traces"
mvn -pl user-service spring-boot:run "-Dspring-boot.run.profiles=dev"
```

For `prod`, also provide required production database environment variables, because prod config intentionally expects external values.

## Testing Option 9: Failure Testing

Stop the collector:

```bash
docker compose stop otel-collector
```

Send a request:

```bash
curl http://localhost:8080/api/users
```

The API should still work. Tracing export may log connection errors, but telemetry should not break business traffic.

Start the collector again:

```bash
docker compose up -d otel-collector
```

Send a fresh request and confirm traces appear again in Zipkin.

## Troubleshooting

If Zipkin has no traces:

- Confirm services are running with the `local` profile.
- Confirm `otel-collector` is running.
- Confirm `MANAGEMENT_OTLP_TRACING_ENDPOINT` points to the right collector URL.
- Send a new request through the gateway after services have started.
- Check service logs for connection errors to port `4318`.
- Check collector logs for `TracesExporter`.
- Check Zipkin is running at `http://localhost:9411`.
- In Docker, use `http://otel-collector:4318/v1/traces`, not `localhost`.
- In Maven/local runs, use `http://localhost:4318/v1/traces`.

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
