package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.domain.QuantityPerProduct;

public record QuantityPerProductRequest(
    Long productId,
    Integer quantity
) {
  public QuantityPerProduct toQuantityPerProduct() {
    return new QuantityPerProduct(this.productId, this.quantity);
  }
}
