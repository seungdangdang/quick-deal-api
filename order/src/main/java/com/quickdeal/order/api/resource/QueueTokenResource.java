package com.quickdeal.order.api.resource;


import com.quickdeal.order.service.domain.QueueToken;

public record QueueTokenResource(
    String userUUID,
    Long queueNumber,
    String jwtToken
) {

  public static QueueTokenResource from(QueueToken token) {
    return new QueueTokenResource(token.userUUID(), token.queueNumber(), token.jwtToken());
  }
}
