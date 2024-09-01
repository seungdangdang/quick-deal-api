package com.quickdeal.purchase.api.resource;

import com.quickdeal.purchase.domain.OrderInfo;
import com.quickdeal.purchase.domain.OrderProductInfo;

public record PaymentPageResource(
    Long id,
    OrderProductInfo products,
    Long timeoutInSeconds
) {

  public static PaymentPageResource from(OrderInfo orderInfo, long timeoutInSeconds) {
    return new PaymentPageResource(orderInfo.id(), orderInfo.products(), timeoutInSeconds);
  }
}
