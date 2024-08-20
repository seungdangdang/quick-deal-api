package com.quickdeal.order.api.resource;

import com.quickdeal.order.domain.OrderCreationCommand;
import com.quickdeal.order.domain.QuantityPerProduct;
import java.util.List;

public record OrderCreateRequestBody(
    String userUUID,
    List<QuantityPerProductRequest> quantityPerProductRequests
) {

  public OrderCreationCommand toCommand() {
    List<QuantityPerProduct> quantityPerProducts = quantityPerProductRequests.stream()
        .map(qpr -> new QuantityPerProduct(
            qpr.productId(),
            qpr.quantity()
        )).toList();
    return new OrderCreationCommand(
        userUUID,
        quantityPerProducts
    );
  }
}
