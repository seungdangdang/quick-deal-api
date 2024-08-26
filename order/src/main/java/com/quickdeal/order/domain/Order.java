package com.quickdeal.order.domain;

import java.time.Instant;

public record Order(
    Long id,
    String userUUID,
    OrderStatusType status,
    Instant createdAt,
    Instant updatedAt
) {

}
