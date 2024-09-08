package com.quickdeal.purchase.inbound.api.resource;

import com.quickdeal.purchase.domain.OrderTicket;

public record OrderTicketResource(
    String value
) {

  public static OrderTicketResource from(OrderTicket ticket) {
    return new OrderTicketResource(ticket.stringToken());
  }
}

