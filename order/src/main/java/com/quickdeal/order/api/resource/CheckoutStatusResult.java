package com.quickdeal.order.api.resource;

public record CheckoutStatusResult(
    CheckoutStatus status,
    long orderId,
    Integer paymentAmount
) {

}
