package com.quickdeal.purchase.domain;

public record QueuePollingCommand(
    Long ticketNumber,
    Long productId,
    String userUUID,
    String jwtToken
) {

}
