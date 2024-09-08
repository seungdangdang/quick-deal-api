package com.quickdeal.purchase.inbound.api.controller;

public record OrderCancelRequestBody(
    String userId,
    Long productId
) {

}
