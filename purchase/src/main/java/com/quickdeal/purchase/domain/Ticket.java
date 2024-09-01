package com.quickdeal.purchase.domain;

public record Ticket(
    Long orderId,
    String jwtToken
) {

}
