package com.quickdeal.order.domain;

public record PaymentCommand(
    Long orderId,
    Long productId
) {

}
