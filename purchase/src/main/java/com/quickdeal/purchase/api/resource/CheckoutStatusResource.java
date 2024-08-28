package com.quickdeal.purchase.api.resource;

import com.quickdeal.purchase.domain.CheckoutStatus;

public record CheckoutStatusResource(
    String status,
    long orderId,
    Integer paymentAmount
) {

  public static CheckoutStatusResource from(CheckoutStatus value) {
    return new CheckoutStatusResource(value.status().toString(), value.orderId(),
        value.paymentAmount());
  }
}
