package com.quickdeal.order.domain;

public record CheckoutStatus(
    CheckoutStatusType status,
    long orderId,
    Integer paymentAmount
) {

}
