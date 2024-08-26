package com.quickdeal.order.domain;

public enum CheckoutStatusType {
  CHECKOUT_COMPLETED,
  CHECKOUT_ERROR,
  ITEM_SOLD_OUT;

  public boolean isCheckoutCompleted() {
    return this == CHECKOUT_COMPLETED;
  }

  public boolean isCheckoutError() {
    return this == CHECKOUT_ERROR;
  }

  public boolean isItemSoldOut() {
    return this == ITEM_SOLD_OUT;
  }
}
