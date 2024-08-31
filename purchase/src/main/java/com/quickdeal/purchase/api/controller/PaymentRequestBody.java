package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.domain.PaymentCommand;

public record PaymentRequestBody(
    String ticketToken,
    Integer paymentAmount
) {

  public PaymentCommand toCommand() {
    return new PaymentCommand(ticketToken, paymentAmount);
  }
}
