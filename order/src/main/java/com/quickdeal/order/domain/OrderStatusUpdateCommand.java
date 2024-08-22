package com.quickdeal.order.domain;

import com.quickdeal.order.infrastructure.entity.OrderStatus;

public record OrderStatusUpdateCommand(
    Long orderId,
    OrderStatus status
) {

}
