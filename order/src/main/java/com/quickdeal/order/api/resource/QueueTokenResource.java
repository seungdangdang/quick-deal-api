package com.quickdeal.order.api.resource;

import com.quickdeal.order.domain.QueueToken;

public record QueueTokenResource(
    Long queueNumber,
    Long productId,
    String userUUID,
    String jwtToken
) {

  public static QueueTokenResource from(QueueToken token) {
    return new QueueTokenResource( token.queueNumber(), token.productId(), token.userUUID(), token.jwtToken());
  }
}
