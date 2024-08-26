package com.quickdeal.purchase.domain;

public record Ticket(
    Long ticketNumber,
    Long productId,
    String userUUID,
    String jwtToken
) {

}
