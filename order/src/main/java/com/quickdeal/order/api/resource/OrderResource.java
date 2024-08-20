package com.quickdeal.order.api.resource;

import com.quickdeal.order.infrastructure.entity.OrderStatus;
import com.quickdeal.order.domain.Order;
import java.time.Instant;

public record OrderResource(
    Long id,
    String userUUID,
    OrderStatus status,
    Instant createdAt,
    Instant updatedAt
) {

  public static OrderResource from(Order order) {
    return new OrderResource(
        order.id(),
        order.userUUID(),
        order.status(),
        order.createdAt(),
        order.updatedAt()
    );
  }
}
