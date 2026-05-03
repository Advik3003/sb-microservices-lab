# Profiles

Each service includes these profiles:

- `local`: runs on the developer machine with Docker Compose dependencies.
- `dev`: example non-production environment.
- `prod`: production-style environment variables and stricter JPA mode.
- `localstack`: local AWS-compatible endpoint settings for future cloud integration practice.

Run a module with a profile:

```bash
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
```

The profile files are intentionally simple so the differences are easy to compare.
