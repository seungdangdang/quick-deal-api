package com.quickdeal.order.domain;

public record QueuePollingCommand(
    Long ticketNumber,
    Long productId,
    String userUUID,
    String jwtToken
) {

}
