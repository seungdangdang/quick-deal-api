package com.quickdeal.order.api.resource;

import com.quickdeal.order.domain.Ticket;

public record QueueTokenResource(
    Long queueNumber,
    Long productId,
    String userUUID,
    String jwtToken
) {

  public static QueueTokenResource from(Ticket token) {
    return new QueueTokenResource(token.ticketNumber(), token.productId(), token.userUUID(),
        token.jwtToken());
  }
}
