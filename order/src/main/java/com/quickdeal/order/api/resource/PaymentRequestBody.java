package com.quickdeal.order.api.resource;

import com.quickdeal.order.domain.PaymentCommand;

public record PaymentRequestBody(
    Long orderId,
    Long productId,
    Integer paymentAmount
) {

  public PaymentCommand toCommand() {
    return new PaymentCommand(orderId, productId, paymentAmount);
  }
}
