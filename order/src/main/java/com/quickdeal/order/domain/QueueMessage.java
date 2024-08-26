package com.quickdeal.order.domain;

public record QueueMessage(
    Long queueNumber,
    Long productId,
    String userUUID,
    String queueToken
) {

}
