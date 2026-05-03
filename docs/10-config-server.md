# Config Server

`config-server` runs on port `8888` and uses Spring Cloud Config Server.

This lab uses the native file backend:

```text
config-repo/
```

Example endpoint:

```text
http://localhost:8888/user-service/local
```

Business services use `spring.config.import=optional:configserver:http://localhost:8888`, so they can start locally even while you are learning and the config server is not running yet.
