package com.quickdeal.purchase.api.resource;

import com.quickdeal.purchase.domain.PaymentCommand;

public record PaymentRequestBody(
    Long orderId,
    Long productId,
    Integer paymentAmount
) {

  public PaymentCommand toCommand() {
    return new PaymentCommand(orderId, productId, paymentAmount);
  }
}
