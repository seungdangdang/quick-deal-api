package com.quickdeal.purchase.domain;

import java.time.Instant;

public record Order(
    Long id,
    String userId,
    OrderStatusType status,
    Instant createdAt,
    Instant updatedAt
) {

}
