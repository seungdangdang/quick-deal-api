package com.quickdeal.order.api.resource;

public record QueuePollingRequestBody(
    Long queueNumber,
    Long productId,
    String userUUID,
    String jwtToken
) {

  public QueuePollingCommand toCommand() {
    return new QueuePollingCommand(
        queueNumber,
        productId,
        userUUID,
        jwtToken
    );
  }
}
