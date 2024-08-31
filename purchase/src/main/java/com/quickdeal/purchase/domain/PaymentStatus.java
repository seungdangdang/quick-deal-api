package com.quickdeal.purchase.domain;

public record PaymentStatus(
    PaymentStatuses status,
    long orderId,
    Integer paymentAmount
) {

  public boolean isPaymentCompleted() {
    return status == PaymentStatuses.PAYMENT_COMPLETED;
  }

  public boolean isPaymentError() {
    return status == PaymentStatuses.PAYMENT_ERROR;
  }

  public boolean isItemSoldOut() {
    return status == PaymentStatuses.ITEM_SOLD_OUT;
  }
}
