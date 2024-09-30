package com.quickdeal.purchase.domain;

public record QuantityPerProduct(
    Long productId,
    Integer quantity
) {
  public com.quickdeal.purchase.domain.QuantityPerProduct toQuantityPerProduct() {
    return new com.quickdeal.purchase.domain.QuantityPerProduct(this.productId, this.quantity);
  }
}
