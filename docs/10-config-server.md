# Config Server

## Why Config Server

Microservices need different settings in different environments: database URLs, feature flags, public gateway URLs, logging levels, and service endpoints. Keeping all settings inside each JAR makes changes harder and encourages rebuilding for configuration-only updates.

Spring Cloud Config Server externalizes configuration so services can load environment-specific settings at startup.

## How It Is Implemented

`config-server` runs on port `8888` and uses the native file backend. The config files live in:

```text
config-repo/
```

Examples:

- `user-service.yml`
- `user-service-local.yml`
- `order-service.yml`
- `api-gateway-local.yml`

Business services use:

```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888
```

`optional` is used for learning convenience. It lets a service start even if the config server is not running. In production, you may remove `optional` so missing central config fails fast.

## How To Try It

Start `config-server` and open:

```text
http://localhost:8888/user-service/local
```

You should see the merged config for `user-service` with the `local` profile.

## Production Notes

This lab uses local files. Real systems often back Config Server with a Git repository, Vault, Kubernetes ConfigMaps and Secrets, or a cloud configuration service. Secrets should not be committed into source code.
