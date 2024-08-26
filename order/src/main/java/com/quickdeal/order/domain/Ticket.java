package com.quickdeal.order.domain;

public record Ticket(
    Long ticketNumber,
    Long productId,
    String userUUID,
    String jwtToken
) {

}
