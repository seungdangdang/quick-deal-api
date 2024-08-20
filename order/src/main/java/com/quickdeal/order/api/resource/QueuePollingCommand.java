package com.quickdeal.order.api.resource;

public record QueuePollingCommand(
    String userUUID,
    Long queueNumber,
    String jwtToken
) {

}
