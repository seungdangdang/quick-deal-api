package com.quickdeal.purchase.api.resource;

import com.quickdeal.purchase.domain.Ticket;

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
