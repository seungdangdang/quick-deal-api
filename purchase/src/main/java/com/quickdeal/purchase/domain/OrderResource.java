package com.quickdeal.purchase.domain;

public record OrderResource(
    Long id,
    OrderProductInfo products
) {

  public static OrderResource from(OrderInfo orderInfo) {
    return new OrderResource(orderInfo.id(), orderInfo.products());
  }
}
