package com.quickdeal.order.api.resource;

public record QueueRequestBody(
    Long productId,
    String userUUID
) {

  public QueueCommand toCommand() {
    return new QueueCommand(productId, userUUID);
  }
}
