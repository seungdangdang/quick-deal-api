package com.quickdeal.purchase.api.controller;

public record OrderCancelRequestBody(
    String userId,
    Long productId
) {

}
