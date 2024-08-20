package com.quickdeal.order.domain;

public record QueueToken(
    Long queueNumber,
    Long productId,
    String userUUID,
    String jwtToken
) {

}
