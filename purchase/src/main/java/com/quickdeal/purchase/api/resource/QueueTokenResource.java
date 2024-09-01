package com.quickdeal.purchase.api.resource;

import com.quickdeal.purchase.domain.Ticket;

public record QueueTokenResource(
    Long orderId,
    String jwtToken
) {

  public static QueueTokenResource from(Ticket token) {
    return new QueueTokenResource(token.orderId(), token.jwtToken());
  }
}
