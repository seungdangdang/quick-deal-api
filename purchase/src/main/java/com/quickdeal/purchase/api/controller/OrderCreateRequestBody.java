package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.domain.OrderCreationCommand;

public record OrderCreateRequestBody(
    String userUUID,
    QuantityPerProductRequest quantityPerProductRequest
) {

  public OrderCreationCommand toCommand() {
    return new OrderCreationCommand(
        this.userUUID(),
        this.quantityPerProductRequest().toQuantityPerProduct()
    );
  }
}
