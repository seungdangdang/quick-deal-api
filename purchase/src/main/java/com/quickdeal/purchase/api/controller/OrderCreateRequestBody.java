package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.domain.OrderCreationCommand;

public record OrderCreateRequestBody(
    String userId,
    QuantityPerProductRequest quantityPerProductRequest
) {

  public OrderCreationCommand toCommand() {
    return new OrderCreationCommand(
        this.userId(),
        this.quantityPerProductRequest().toQuantityPerProduct()
    );
  }
}
