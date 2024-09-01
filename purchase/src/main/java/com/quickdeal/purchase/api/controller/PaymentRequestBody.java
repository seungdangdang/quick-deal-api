package com.quickdeal.purchase.api.controller;

public record PaymentRequestBody(
    String userId,
    Long orderId,
    Integer paymentAmount
) {

}
