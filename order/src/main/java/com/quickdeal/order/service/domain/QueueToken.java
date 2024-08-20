package com.quickdeal.order.service.domain;

public record QueueToken(
    Long queueNumber,
    Long productId,
    String userUUID,
    String jwtToken
) {

}
