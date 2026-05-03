package com.example.microservices.order.dto;

public record UserSummary(
        Long id,
        String name,
        String email,
        String phone
) {
}
