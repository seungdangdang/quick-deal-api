package com.quickdeal.purchase.domain;

public record CheckoutStatus(
    CheckoutStatusType status,
    long orderId,
    Integer paymentAmount
) {

}
