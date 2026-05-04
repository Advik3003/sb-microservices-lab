# ELK And Distributed Logging

## Hinglish: ELK Ko Simple Language Me Samjho

Socho aapke paas 5 alag-alag shops hain: `api-gateway`, `user-service`, `order-service`, `config-server`, aur `registry-server`. Har shop apni diary me likh rahi hai ki kya hua. Agar customer bole "mera order fail hua", to aapko 5 diaries kholkar manually dekhna padega. Ye painful hai.

ELK ka idea simple hai: sab diaries ki entries ek central jagah bhej do, jahan search karna easy ho.

ELK ka full form:

- Elasticsearch: Ye bada searchable storage hai. Logs ko store aur index karta hai.
- Logstash: Ye beech ka processor hai. Logs ko clean, parse, enrich karta hai.
- Kibana: Ye UI hai. Isme search, dashboard, graph, filters dekhte hain.

Easy analogy:

```text
Application logs = CCTV footage
Elasticsearch = central hard disk
Logstash = video processor
Kibana = screen jahan aap footage search karte ho
```

## Hinglish: Microservices Me ELK Kyu Chahiye

Monolith me ek hi app hoti hai, logs ek jagah mil jate hain. Microservices me ek request multiple services se guzarti hai.

Example:

```text
User -> api-gateway -> order-service -> user-service -> database
```

Agar order create fail hua, to problem gateway me ho sakti hai, order service me ho sakti hai, user service call me ho sakti hai, ya database me ho sakti hai. ELK central search deta hai:

```text
service=order-service ERROR
traceId=abc123
endpoint=/api/orders
```

Isse debugging fast hoti hai.

## Hinglish: Is Project Me ELK Ka Equivalent Kya Hai

Is project me currently ELK nahi chal raha. Iska lightweight local alternative use ho raha hai:

```text
Spring Boot logs -> Promtail -> Loki -> Grafana
```

ELK version hota:

```text
Spring Boot logs -> Filebeat -> Elasticsearch -> Kibana
```

Ya parsing chahiye to:

```text
Spring Boot logs -> Filebeat -> Logstash -> Elasticsearch -> Kibana
```

Promtail/Loki local learning ke liye halka hai. ELK enterprise search ke liye powerful hai, but thoda heavy hota hai.

## Hinglish Tutorial: ELK Implement Kaise Karen

Step 1: Application logs ready karo.

Spring Boot services already file logs likh rahe hain:

```text
logs/api-gateway.log
logs/user-service.log
logs/order-service.log
```

Step 2: Filebeat add karo.

Filebeat ka kaam hai log files read karke Elasticsearch ya Logstash ko bhejna.

Example flow:

```text
logs/*.log -> Filebeat -> Elasticsearch
```

Step 3: Elasticsearch run karo.

Elasticsearch logs ko searchable format me store karega.

Step 4: Kibana run karo.

Kibana UI me jaake search kar sakte ho:

```text
ERROR
service=user-service
trace=abc123
```

Step 5: Dashboard banao.

Useful dashboards:

- errors by service
- slow endpoints
- validation failures
- logs by trace id
- logs after deployment

## Hinglish Testing Examples

Error generate karo:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"\",\"email\":\"bad-email\",\"phone\":\"1\"}"
```

Kibana me search:

```text
Validation failed
```

Service-specific search:

```text
service=user-service
```

Trace id ke saath search:

```text
trace=69f7774ace7b77f2d8532b549ec10f36
```

Layman rule: jab bhi bug aaye, pehle service name, timestamp, error keyword, aur trace id se search karo.

## Why Distributed Logging

In microservices, one user request may touch several services. If logs stay only inside each service container or local file, debugging becomes slow because you must open many places and manually connect events.

Distributed logging collects logs from all services into one searchable system. You can filter by service, timestamp, endpoint, error level, trace id, or user-facing request id.

## What ELK Means

ELK usually means:

- Elasticsearch: stores and indexes logs.
- Logstash: processes and enriches logs.
- Kibana: searches and visualizes logs.

Many modern setups use Elastic Stack with Beats or Elastic Agent instead of Logstash for simple collection.

## How To Implement ELK Locally

A local ELK flow can look like this:

1. Spring Boot services write logs to files or stdout.
2. Filebeat reads those logs.
3. Filebeat sends logs to Logstash or directly to Elasticsearch.
4. Kibana searches and visualizes the logs.

For this project, the closest ELK equivalent to the current Loki setup would be:

```text
Spring Boot log files -> Filebeat -> Elasticsearch -> Kibana
```

If you need parsing or enrichment, add Logstash:

```text
Spring Boot log files -> Filebeat -> Logstash -> Elasticsearch -> Kibana
```

## How To Implement ELK In Production

In production containers, prefer stdout logs. The platform collects stdout and ships it using an agent.

Common production flows:

```text
Container stdout -> Filebeat or Elastic Agent -> Elasticsearch -> Kibana
```

```text
Container stdout -> Fluent Bit -> Logstash -> Elasticsearch -> Kibana
```

Important production practices:

- Use JSON logs if possible.
- Include service name, environment, version, and trace id.
- Avoid logging secrets, tokens, passwords, or full payment data.
- Set retention policies so log storage does not grow forever.
- Add alerts for repeated errors and high error rates.
- Connect logs with metrics and traces for faster debugging.

## Other Approaches For Distributed Logs

Yes, there are several good alternatives.

Loki, Promtail, and Grafana:
This repo currently uses this approach. Loki stores log labels and compressed log streams, which is usually lighter than indexing every log field. It is excellent for local learning and cost-conscious production setups.

ELK or Elastic Stack:
Good when you need powerful full-text search, rich parsing, mature dashboards, and broad ecosystem support. It can be heavier to run than Loki.

OpenTelemetry Collector:
Good when you want one vendor-neutral collector for logs, metrics, and traces. It can export to Elastic, Loki, Datadog, New Relic, Grafana Cloud, or cloud-native backends.

Cloud-native logging:
AWS CloudWatch Logs, Azure Monitor, and Google Cloud Logging are common in managed cloud environments. They reduce operational overhead but tie you to a cloud provider.

Fluent Bit or Fluentd:
Good lightweight collectors. Fluent Bit is common in Kubernetes because it is efficient and easy to run as a DaemonSet.

## Which Should You Use

For local learning, use Loki plus Grafana or a small ELK Compose stack. Loki is simpler and lighter. ELK teaches a very common enterprise stack but uses more memory.

For production, choose based on your platform:

- Kubernetes with Grafana stack: Loki, Promtail or Grafana Agent, Grafana.
- Enterprise search-heavy logging: Elastic Stack.
- Cloud-first deployment: CloudWatch, Azure Monitor, or Google Cloud Logging.
- Vendor-neutral observability: OpenTelemetry Collector with your chosen backend.

The implementation idea is always the same: applications emit useful structured logs, agents collect them, a backend stores them, and dashboards make them searchable.
