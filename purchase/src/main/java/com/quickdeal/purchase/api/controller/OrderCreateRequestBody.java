package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.domain.OrderCreationCommand;
import com.quickdeal.purchase.domain.QuantityPerProduct;

public record OrderCreateRequestBody(
    String userId,
    QuantityPerProduct quantityPerProduct
) {

  public OrderCreationCommand toCommand() {
    return new OrderCreationCommand(
        this.userId(),
        this.quantityPerProduct().toQuantityPerProduct()
    );
  }
}
