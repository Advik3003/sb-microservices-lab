package com.example.microservices.user.dto;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone
) {
}
