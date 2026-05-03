# Zipkin Distributed Tracing

## Why Distributed Tracing

Logs tell you what happened inside one service. Distributed tracing shows how one request moves across services.

For example, a request can start at `api-gateway`, route to `order-service`, and then call `user-service`. Without tracing, you must manually connect log lines from three services. With tracing, Zipkin shows the full request path as one trace.

Distributed tracing helps answer:

- Which services handled this request?
- Which service was slow?
- Did a downstream service fail?
- How long did each service call take?
- Are trace ids present in logs for deeper debugging?

## What Zipkin Does

Zipkin stores and displays traces. A trace is the full journey of one request. A span is one timed operation inside that journey.

In this lab:

- `api-gateway` creates or continues a trace for incoming requests.
- `order-service` contributes spans for order API handling.
- `user-service` contributes spans when it is called by the order service.
- Zipkin receives spans at `http://localhost:9411/api/v2/spans`.

## How It Is Implemented

`docker-compose.yml` starts Zipkin:

```yaml
zipkin:
  image: openzipkin/zipkin:3.4
  ports:
    - "9411:9411"
```

The request path services include these dependencies:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

`micrometer-tracing-bridge-brave` creates trace and span data. `zipkin-reporter-brave` sends that data to Zipkin.

Each traced service has:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

`probability: 1.0` means every request is traced. That is useful for local learning. In production, use a lower value such as `0.1` or configure sampling based on traffic and cost.

## How Logs And Traces Connect

The service logback files already include:

```text
trace=%X{traceId:-none} span=%X{spanId:-none}
```

After tracing is enabled, Micrometer puts `traceId` and `spanId` into the logging context. That lets you find a trace in Zipkin and then search the same trace id in logs.

## How To Try It

Start Zipkin:

```bash
docker compose up -d zipkin
```

Run the services, then send a request through the gateway:

```bash
curl http://localhost:8080/api/orders
```

Open:

```text
http://localhost:9411
```

Click "Run Query". You should see traces from `api-gateway`, `order-service`, and any downstream calls triggered by your request.

To see a multi-service trace, create an order or update an order because those flows call `user-service`.

## Production Notes

Zipkin is good for learning and can be used in production, but many teams now use OpenTelemetry for instrumentation and export traces to Zipkin, Jaeger, Tempo, Datadog, New Relic, or a cloud tracing service.

Recommended production practices:

- Do not sample every request at high traffic unless you can afford it.
- Propagate trace headers through gateways and service clients.
- Include trace ids in logs.
- Add alerts from metrics, then use traces and logs to investigate.
- Protect tracing UIs because traces may contain URLs or metadata.
