package com.quickdeal.order.api.resource;

public record QueuePollingRequestBody(
    String userUUID,
    Long queueNumber,
    String jwtToken
) {

  public QueuePollingCommand toCommand() {
    return new QueuePollingCommand(
        userUUID,
        queueNumber,
        jwtToken
    );
  }
}
