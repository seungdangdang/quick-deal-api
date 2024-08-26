package com.quickdeal.order.api.resource;

public enum CheckoutStatus {
  DONE_CHECKOUT,
  ERROR;

  public boolean isDoneCheckout() {
    return this == CheckoutStatus.DONE_CHECKOUT;
  }

  public boolean isError() {
    return this == CheckoutStatus.ERROR;
  }
}
