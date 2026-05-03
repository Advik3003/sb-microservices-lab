# Profiles

## Why Profiles

The same service usually needs different settings in different environments. A local developer machine uses localhost ports. A dev server might use internal hostnames. Production should use environment variables and stricter safety settings.

Spring profiles let one application keep environment-specific configuration in separate files.

## How It Is Implemented

Each service includes:

- `application-local.yml`
- `application-dev.yml`
- `application-prod.yml`
- `application-localstack.yml`

The profiles mean:

- `local`: developer machine with Docker Compose dependencies.
- `dev`: example non-production environment.
- `prod`: production-style environment variables and stricter JPA settings.
- `localstack`: local AWS-compatible endpoint settings for future cloud practice.

## How To Run With A Profile

Run a module with:

```bash
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
```

Run the gateway with:

```bash
mvn -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

## Production Notes

Avoid storing real passwords in profile files. Use environment variables, Vault, Kubernetes Secrets, cloud secrets managers, or your deployment platform's secret store.
