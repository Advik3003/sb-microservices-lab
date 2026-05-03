# Local and Centralised Logs

Local logs are written by `logback-spring.xml` in each business service.

The services write to:

- `logs/user-service.log`
- `logs/order-service.log`

Centralised logging is represented with Promtail, Loki, and Grafana in `docker-compose.yml`.

Promtail reads files from `./logs`, sends log lines to Loki, and Grafana can be used to query them. This demonstrates the common production idea of keeping services stateless while collecting logs in one searchable place.
