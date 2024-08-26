package com.quickdeal.purchase.domain;

public record QueueMessage(
    Long ticketNumber,
    Long productId,
    String userUUID,
    String ticketToken
) {

}
