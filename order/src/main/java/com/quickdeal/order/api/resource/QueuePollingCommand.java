package com.quickdeal.order.api.resource;

public record QueuePollingCommand(
    Long queueNumber,
    Long productId,
    String userUUID,
    String jwtToken
) {

}
