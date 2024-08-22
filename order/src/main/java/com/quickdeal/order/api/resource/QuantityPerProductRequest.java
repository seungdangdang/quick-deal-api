package com.quickdeal.order.api.resource;

import com.quickdeal.order.domain.QuantityPerProduct;

public record QuantityPerProductRequest(
    Long productId,
    Integer quantity
) {
  public QuantityPerProduct toQuantityPerProduct() {
    return new QuantityPerProduct(this.productId, this.quantity);
  }
}
