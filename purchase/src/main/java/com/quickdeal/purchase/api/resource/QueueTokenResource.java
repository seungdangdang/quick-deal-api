package com.quickdeal.purchase.api.resource;

import com.quickdeal.purchase.domain.Ticket;

public record QueueTokenResource(
    Long orderId,
    String ticketToken
) {

  public static QueueTokenResource from(Ticket token) {
    return new QueueTokenResource(token.orderId(), token.ticketToken());
  }
}
