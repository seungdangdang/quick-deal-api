package com.quickdeal.purchase.domain;

public record QueueCommand(
    Long productId,
    String userUUID
) {

}
