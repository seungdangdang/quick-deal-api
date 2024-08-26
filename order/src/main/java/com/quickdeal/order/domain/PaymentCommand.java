package com.quickdeal.order.domain;

public record PaymentCommand(
    long orderId,
    long productId,
    int paymentAmount
) {

}
