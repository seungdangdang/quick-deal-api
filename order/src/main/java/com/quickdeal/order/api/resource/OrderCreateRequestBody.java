package com.quickdeal.order.api.resource;

import com.quickdeal.order.domain.OrderCreationCommand;

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
