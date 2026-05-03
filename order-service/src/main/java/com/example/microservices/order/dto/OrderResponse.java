package com.example.microservices.order.dto;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        Long userId,
        String productName,
        Integer quantity,
        BigDecimal price
) {
}
