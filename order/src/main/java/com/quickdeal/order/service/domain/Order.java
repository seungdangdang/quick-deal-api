package com.quickdeal.order.service.domain;

import com.quickdeal.order.infrastructure.entity.OrderStatus;
import java.time.Instant;

public record Order(
    Long id,
    String userUUID,
    OrderStatus status,
    Instant createdAt,
    Instant updatedAt
) {

}
