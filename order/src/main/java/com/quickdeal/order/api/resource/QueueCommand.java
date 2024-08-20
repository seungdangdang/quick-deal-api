package com.quickdeal.order.api.resource;

public record QueueCommand(
    Long productId,
    String userUUID
) {

}
