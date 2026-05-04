# Zipkin Trace Visualization

## Hinglish: Zipkin Ko Simple Language Me Samjho

Zipkin ek tracing UI hai. Ye batata hai ki ek request system ke andar kaun-kaun si services se guzri, kis service ne kitna time liya, aur request kaha slow ya fail hui.

Simple analogy:

```text
Online food order tracking
```

Jab aap food order karte ho, app dikhata hai:

```text
Order placed -> Restaurant accepted -> Food prepared -> Rider picked -> Delivered
```

Zipkin microservices ke liye same tracking karta hai:

```text
api-gateway -> order-service -> user-service -> response
```

Trace ka matlab full journey. Span ka matlab journey ka ek step.

```text
Trace = poori request ki kahani
Span = kahani ka ek scene
```

## Hinglish: Zipkin Kyu Use Karte Hain

Logs me aapko individual service ki story milti hai. Zipkin me aapko request ki full journey milti hai.

Example problem:

User bolta hai: "Order create slow hai."

Without Zipkin:

- gateway logs dekho
- order logs dekho
- user logs dekho
- manually timestamp match karo

With Zipkin:

- Zipkin open karo
- trace dekho
- immediately pata chalega kaunsa span slow hai

Example:

```text
api-gateway: 20 ms
order-service: 900 ms
user-service: 50 ms
```

Yaha slow part `order-service` hai.

## Hinglish: Is Project Me Zipkin Kaise Connected Hai

Services direct Zipkin ko traces nahi bhejti. Professional setup me services OpenTelemetry Collector ko data bhejti hain, collector Zipkin ko forward karta hai.

Flow:

```text
api-gateway/user-service/order-service
  -> OpenTelemetry Collector
  -> Zipkin
```

Local URLs:

- OpenTelemetry Collector: `http://localhost:4318/v1/traces`
- Zipkin UI: `http://localhost:9411`

## Hinglish Tutorial: Zipkin Test Kaise Kare

Step 1: Infra start karo.

```bash
docker compose up -d zipkin otel-collector
```

Step 2: Services run karo.

```bash
mvn -pl registry-server spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl config-server spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl order-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

Step 3: Request bhejo.

```bash
curl http://localhost:8080/api/users
curl http://localhost:8080/api/orders
```

Step 4: Multi-service trace generate karo.

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Zipkin User\",\"email\":\"zipkin.user@example.com\",\"phone\":\"9876543210\"}"
```

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"productName\":\"Zipkin Book\",\"quantity\":1,\"price\":299.00}"
```

Step 5: Zipkin open karo.

```text
http://localhost:9411
```

Click `Run Query`. Aapko services dikhni chahiye:

- `api-gateway`
- `order-service`
- `user-service`
- `config-server`
- `registry-server`

Step 6: Trace open karo.

Dekho kaunsi service kitna time le rahi hai. Agar red/error span hai to wahi failure point hai.

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

In the current project, services do not send spans directly to Zipkin. They export traces with OpenTelemetry OTLP to the local OpenTelemetry Collector, and the collector forwards those traces to Zipkin.

In this lab:

- `api-gateway` creates or continues a trace for incoming requests.
- `order-service` contributes spans for order API handling.
- `user-service` contributes spans when it is called by the order service.
- OpenTelemetry Collector receives OTLP spans at `http://localhost:4318/v1/traces`.
- Zipkin receives collector-forwarded spans at `http://localhost:9411/api/v2/spans`.

## How It Is Implemented

`docker-compose.yml` starts Zipkin:

```yaml
zipkin:
  image: openzipkin/zipkin:3.4
  ports:
    - "9411:9411"
```

It also starts the OpenTelemetry Collector:

```yaml
otel-collector:
  image: otel/opentelemetry-collector-contrib:0.111.0
  ports:
    - "4317:4317"
    - "4318:4318"
```

Each traced service has:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
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
docker compose up -d zipkin otel-collector
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

Zipkin is good for learning and can be used in production, but many teams now use OpenTelemetry for instrumentation and send traces through an OpenTelemetry Collector to Zipkin, Jaeger, Tempo, Datadog, New Relic, or a cloud tracing service.

Recommended production practices:

- Do not sample every request at high traffic unless you can afford it.
- Propagate trace headers through gateways and service clients.
- Include trace ids in logs.
- Add alerts from metrics, then use traces and logs to investigate.
- Protect tracing UIs because traces may contain URLs or metadata.
