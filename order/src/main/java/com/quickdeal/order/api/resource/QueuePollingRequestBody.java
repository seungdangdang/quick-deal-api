package com.quickdeal.order.api.resource;

import com.quickdeal.order.domain.QueuePollingCommand;

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
