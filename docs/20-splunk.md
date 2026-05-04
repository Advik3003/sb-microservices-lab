# Splunk

## Hinglish: Splunk Ko Simple Language Me Samjho

Splunk ek powerful search engine hai machine data ke liye. Machine data ka matlab application logs, server logs, audit events, errors, metrics, traces, security events, etc.

Simple analogy:

```text
Google for application/server logs
```

Jaise Google me aap web pages search karte ho, Splunk me aap logs/events search karte ho.

Example:

```text
Mujhe user-service ke ERROR logs dikhao
Mujhe trace id abc123 ki full log journey dikhao
Mujhe last 1 hour me kitne validation errors aaye dikhao
```

Splunk me search language ko SPL bolte hain: Search Processing Language.

## Hinglish: Splunk Kyu Use Karte Hain

Microservices me logs multiple jagah generate hote hain:

```text
api-gateway logs
user-service logs
order-service logs
config-server logs
registry-server logs
```

Agar production me issue aaye, manually har server/container me jaake logs check karna slow hai. Splunk central jagah deta hai:

```text
All logs -> Splunk -> Search/Dashboard/Alerts
```

Splunk se aap:

- errors search kar sakte ho
- dashboard bana sakte ho
- alerts setup kar sakte ho
- trace id se logs correlate kar sakte ho
- security/audit logs analyze kar sakte ho

## Hinglish: Splunk Is Project Me Kaha Fit Hota Hai

Current project me local stack ye hai:

```text
Logs: Spring Boot -> log files -> Promtail -> Loki -> Grafana
Traces: Spring Boot -> OpenTelemetry Collector -> Zipkin
```

Splunk use karna ho to flow aisa ho sakta hai:

```text
Logs: Spring Boot -> OpenTelemetry Collector / Fluent Bit / Universal Forwarder -> Splunk
Traces: Spring Boot -> OpenTelemetry Collector -> Splunk Observability Cloud
```

Splunk Enterprise mostly logs/search ke liye use hota hai. Splunk Observability Cloud traces/metrics/APM ke liye use hota hai.

## Hinglish Tutorial: Splunk Local Me Kaise Try Kare

Step 1: Splunk container start karo.

```bash
docker run -d \
  --name splunk \
  -p 8000:8000 \
  -p 8088:8088 \
  -e SPLUNK_START_ARGS=--accept-license \
  -e SPLUNK_PASSWORD=Password123! \
  splunk/splunk:latest
```

Step 2: Splunk UI open karo.

```text
http://localhost:8000
```

Login:

- username: `admin`
- password: `Password123!`

Step 3: HEC enable karo.

HEC ka full form hai HTTP Event Collector. Ye HTTP endpoint hai jahan logs/events bheje ja sakte hain.

Splunk UI me:

1. Settings open karo.
2. Data Inputs open karo.
3. HTTP Event Collector select karo.
4. Global Settings me HEC enable karo.
5. Port `8088` rakho.

Step 4: Token create karo.

1. HTTP Event Collector me `New Token`.
2. Name: `spring-boot-microservices`.
3. Index: `main`.
4. Token copy karo.

Step 5: Test event bhejo.

```bash
curl -k http://localhost:8088/services/collector \
  -H "Authorization: Splunk <HEC_TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"event\":\"hello from spring boot lab\",\"sourcetype\":\"manual\"}"
```

Step 6: Splunk me search karo.

```spl
index=main "hello from spring boot lab"
```

Agar result aa gaya, HEC working hai.

## Hinglish: Application Logs Splunk Me Kaise Bheje

Option 1: Universal Forwarder.

```text
logs/*.log -> Splunk Universal Forwarder -> Splunk
```

Ye Splunk-native approach hai.

Option 2: OpenTelemetry Collector.

```text
logs/*.log -> OpenTelemetry Collector -> Splunk HEC
```

Ye modern approach hai because same collector traces, metrics, logs sab handle kar sakta hai.

Option 3: Fluent Bit.

```text
container stdout -> Fluent Bit -> Splunk HEC
```

Docker/Kubernetes me common hai.

## Hinglish SPL Search Examples

All Spring Boot logs:

```spl
index=main source="spring-boot"
```

Only errors:

```spl
index=main ERROR
```

User service logs:

```spl
index=main service=user-service
```

Trace id se search:

```spl
index=main trace=abc123
```

Validation failures:

```spl
index=main "Validation failed"
```

Errors count by service:

```spl
index=main ERROR
| stats count by service
```

Layman tip: Splunk me search karte time pehle broad search karo, phir service, error, trace id, time range se narrow karo.

## Hinglish Testing: Splunk Integration Kaise Verify Kare

Test 1: Splunk UI open ho raha hai?

```text
http://localhost:8000
```

Test 2: HEC test event searchable hai?

```spl
index=main "hello from spring boot lab"
```

Test 3: App me error generate karo.

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"\",\"email\":\"bad-email\",\"phone\":\"1\"}"
```

Splunk search:

```spl
index=main "Validation failed"
```

Test 4: Trace id correlation.

Log me trace id dekho:

```text
trace=<trace-id>
```

Splunk me search:

```spl
index=main trace=<trace-id>
```

Zipkin ya Splunk Observability me same trace open karo. Isse logs aur traces connect ho jate hain.

## What Is Splunk

Splunk is an observability and log analytics platform. It collects machine data such as application logs, metrics, traces, audit events, and infrastructure events, then makes that data searchable through Splunk Search Processing Language, usually called SPL.

In microservices, Splunk is commonly used to answer questions like:

- Which service returned errors?
- What happened for one request id or trace id?
- Which endpoint is slow?
- How many validation failures happened today?
- Did a deployment increase error rates?

Splunk can be used for logs only, but in professional systems it is often part of a wider observability setup with logs, metrics, alerts, dashboards, and sometimes traces.

## Where Splunk Fits In This Project

This project currently uses:

- Spring Boot logs written by Logback.
- OpenTelemetry traces exported to the OpenTelemetry Collector.
- Zipkin for local trace visualization.
- Loki and Grafana for local centralised logs.

Splunk can replace or sit beside Loki/Grafana for logs. A common flow is:

```text
Spring Boot service logs -> Splunk Universal Forwarder / OpenTelemetry Collector / Fluent Bit -> Splunk
```

For traces, a common professional flow is:

```text
Spring Boot services -> OpenTelemetry Collector -> Splunk Observability Cloud
```

Splunk Enterprise is usually used for logs and searching. Splunk Observability Cloud is usually used for metrics, traces, dashboards, and APM.

## Implementation Options

### Option 1: Send Log Files With Splunk Universal Forwarder

This is a classic Splunk approach.

Flow:

```text
logs/*.log -> Splunk Universal Forwarder -> Splunk Enterprise
```

Use this when:

- Your apps write log files.
- You already use Splunk Enterprise.
- You want a mature Splunk-native log forwarding path.

### Option 2: Send Logs Through OpenTelemetry Collector

This is a modern vendor-neutral approach.

Flow:

```text
Spring Boot logs / container stdout -> OpenTelemetry Collector -> Splunk HEC
```

HEC means HTTP Event Collector. It is Splunk's HTTP endpoint for ingesting events.

Use this when:

- You already use OpenTelemetry.
- You want one collector for logs, metrics, and traces.
- You want to avoid installing a Splunk-specific agent in every service.

### Option 3: Send Container Logs With Fluent Bit

This is common in Docker and Kubernetes.

Flow:

```text
Container stdout -> Fluent Bit -> Splunk HEC
```

Use this when:

- Services run in containers.
- You want lightweight log collection.
- Your platform already uses Fluent Bit.

## Step By Step: Local Splunk Enterprise Setup

### Step 1: Start Splunk

Add a Splunk service to Docker Compose or run it directly:

```bash
docker run -d \
  --name splunk \
  -p 8000:8000 \
  -p 8088:8088 \
  -e SPLUNK_START_ARGS=--accept-license \
  -e SPLUNK_PASSWORD=Password123! \
  splunk/splunk:latest
```

Ports:

- `8000`: Splunk Web UI.
- `8088`: HTTP Event Collector.

Open Splunk:

```text
http://localhost:8000
```

Login:

- username: `admin`
- password: `Password123!`

### Step 2: Enable HTTP Event Collector

In Splunk Web:

1. Go to `Settings`.
2. Open `Data Inputs`.
3. Choose `HTTP Event Collector`.
4. Click `Global Settings`.
5. Enable HEC.
6. Keep HTTP port as `8088`.
7. Save.

### Step 3: Create HEC Token

In Splunk Web:

1. Go to `Settings`.
2. Open `Data Inputs`.
3. Choose `HTTP Event Collector`.
4. Click `New Token`.
5. Name it `spring-boot-microservices`.
6. Choose an index, for example `main`.
7. Finish and copy the token.

You will use this token from your collector or log shipper.

## Step By Step: Send Logs With OpenTelemetry Collector

This project already has an OpenTelemetry Collector for traces. To send logs to Splunk, add a Splunk HEC exporter to the collector config.

Example collector config:

```yaml
receivers:
  filelog:
    include:
      - /var/log/microservices/*.log
    start_at: beginning
  otlp:
    protocols:
      grpc:
      http:

processors:
  batch:

exporters:
  splunk_hec:
    token: "${SPLUNK_HEC_TOKEN}"
    endpoint: "http://splunk:8088/services/collector"
    source: "spring-boot"
    sourcetype: "_json"
    index: "main"
    tls:
      insecure_skip_verify: true

service:
  pipelines:
    logs:
      receivers: [filelog]
      processors: [batch]
      exporters: [splunk_hec]
```

What each important part means:

- `filelog`: reads log files.
- `include`: tells the collector which files to read.
- `start_at: beginning`: useful locally so old logs are ingested too.
- `splunk_hec`: sends events to Splunk HTTP Event Collector.
- `token`: authenticates to Splunk.
- `endpoint`: Splunk HEC URL.
- `source`: logical source name for events.
- `sourcetype`: tells Splunk how to interpret events.
- `index`: Splunk index where logs are stored.
- `logs pipeline`: connects log receiver, processor, and exporter.

For production, keep `SPLUNK_HEC_TOKEN` in a secret manager, not in source code.

## Step By Step: Send Traces To Splunk Observability Cloud

For tracing, keep services exporting OTLP to the OpenTelemetry Collector:

```text
Spring Boot services -> OTLP -> OpenTelemetry Collector
```

Then configure the collector to export to Splunk Observability Cloud.

Typical exporter:

```yaml
exporters:
  signalfx:
    access_token: "${SPLUNK_ACCESS_TOKEN}"
    realm: "${SPLUNK_REALM}"

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [signalfx]
```

Important values:

- `SPLUNK_ACCESS_TOKEN`: token from Splunk Observability Cloud.
- `SPLUNK_REALM`: Splunk realm, such as `us0`, `us1`, or `eu0`.

This is better than adding Splunk-specific tracing code to every service because only the collector changes.

## How To Search Logs In Splunk

After logs are ingested, open Splunk Search and try:

```spl
index=main source="spring-boot"
```

Search errors:

```spl
index=main source="spring-boot" ERROR
```

Search one service:

```spl
index=main service=user-service
```

Search by trace id:

```spl
index=main trace=<trace-id>
```

Search validation failures:

```spl
index=main "Validation failed"
```

Count errors by service:

```spl
index=main ERROR
| stats count by service
```

## How To Test Splunk Integration

### Test 1: Confirm Splunk Is Running

Open:

```text
http://localhost:8000
```

Login as `admin`.

### Test 2: Confirm HEC Is Reachable

Send a test event:

```bash
curl -k http://localhost:8088/services/collector \
  -H "Authorization: Splunk <HEC_TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"event\":\"hello from curl\",\"sourcetype\":\"manual\"}"
```

Then search:

```spl
index=main "hello from curl"
```

### Test 3: Generate Application Logs

Call the gateway:

```bash
curl http://localhost:8080/api/users
curl http://localhost:8080/api/orders
```

Create a validation error:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"\",\"email\":\"bad-email\",\"phone\":\"1\"}"
```

Search in Splunk:

```spl
index=main "Validation failed"
```

### Test 4: Correlate Logs With Traces

Find a `traceId` in service logs:

```text
trace=<trace-id>
```

Search the same id in Splunk:

```spl
index=main trace=<trace-id>
```

Then open the same trace in your tracing backend, such as Zipkin or Splunk Observability Cloud.

## Recommended Production Setup

For production, prefer:

```text
Spring Boot services
  -> stdout JSON logs
  -> platform log collector
  -> Splunk HEC
```

And for traces:

```text
Spring Boot services
  -> OTLP
  -> OpenTelemetry Collector
  -> Splunk Observability Cloud
```

Recommendations:

- Do not store Splunk tokens in Git.
- Use JSON logs for easier parsing.
- Include `service`, `environment`, `traceId`, and `spanId`.
- Use separate indexes for dev, staging, and production.
- Configure retention policies.
- Add alerts for error rate, latency, and missing logs.
- Avoid logging secrets, passwords, tokens, and personal data.

## Splunk Versus Current Local Stack

Current local stack:

```text
Logs: Spring Boot -> files -> Promtail -> Loki -> Grafana
Traces: Spring Boot -> OTLP -> Collector -> Zipkin
```

Splunk alternative:

```text
Logs: Spring Boot -> collector/forwarder -> Splunk
Traces: Spring Boot -> OTLP -> Collector -> Splunk Observability Cloud
```

Use Loki/Grafana when you want a lightweight open-source local stack. Use Splunk when your team or organization already standardizes on Splunk for search, alerting, compliance, and operational dashboards.
