package com.quickdeal.purchase.inbound.api.controller;

public record PaymentRequestBody(
    String userId,
    Long orderId,
    Integer paymentAmount
) {

}
