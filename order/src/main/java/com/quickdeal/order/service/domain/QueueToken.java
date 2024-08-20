package com.quickdeal.order.service.domain;

public record QueueToken(
    String userUUID,
    Long queueNumber,
    String jwtToken
) {

}
