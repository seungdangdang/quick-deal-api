package com.quickdeal.purchase.domain;

public record PaymentCommand(
    long orderId,
    long productId,
    int paymentAmount
) {

}
