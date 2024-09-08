package com.quickdeal.purchase.inbound.api.resource;

import com.quickdeal.purchase.domain.PaymentStatus;

public record PaymentStatusResource(
    String status,
    long orderId,
    Integer paymentAmount
) {

  public static PaymentStatusResource from(PaymentStatus value) {
    return new PaymentStatusResource(value.status().toString(), value.orderId(),
        value.paymentAmount());
  }
}
