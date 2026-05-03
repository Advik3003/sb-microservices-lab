package com.example.microservices.order.service;

import com.example.microservices.order.client.UserClient;
import com.example.microservices.order.dto.UserSummary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserLookupService {

    private final UserClient userClient;
    private final RestTemplate restTemplate;

    public UserLookupService(UserClient userClient, RestTemplate restTemplate) {
        this.userClient = userClient;
        this.restTemplate = restTemplate;
    }

    public UserSummary findWithFeign(Long userId) {
        return userClient.findById(userId);
    }

    public UserSummary findWithRestTemplate(Long userId) {
        return restTemplate.getForObject("http://user-service/api/users/{id}", UserSummary.class, userId);
    }
}
