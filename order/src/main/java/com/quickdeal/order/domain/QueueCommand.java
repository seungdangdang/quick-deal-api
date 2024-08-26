package com.quickdeal.order.domain;

public record QueueCommand(
    Long productId,
    String userUUID
) {

}
