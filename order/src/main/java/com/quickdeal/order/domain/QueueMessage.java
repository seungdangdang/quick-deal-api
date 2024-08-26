package com.quickdeal.order.domain;

public record QueueMessage(
    Long ticketNumber,
    Long productId,
    String userUUID,
    String ticketToken
) {

}
