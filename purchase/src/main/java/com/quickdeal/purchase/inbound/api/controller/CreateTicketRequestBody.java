package com.quickdeal.purchase.inbound.api.controller;

public record CreateTicketRequestBody(
    String userId,
    Long productId
) {
}
