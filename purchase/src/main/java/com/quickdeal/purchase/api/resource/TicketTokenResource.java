package com.quickdeal.purchase.api.resource;

import com.quickdeal.purchase.domain.Ticket;

public record TicketTokenResource(
    Long orderId,
    String ticketToken
) {

  public static TicketTokenResource from(Ticket token) {
    return new TicketTokenResource(token.orderId(), token.ticketToken());
  }
}
