# RestTemplate

`RestTemplate` is the classic Spring HTTP client.

In this lab, `order-service` defines a load-balanced `RestTemplate` bean. That allows calls like:

```java
http://user-service/api/users/{id}
```

The `user-service` name is resolved through Eureka. The update-order flow uses this path so you can compare RestTemplate with Feign.
