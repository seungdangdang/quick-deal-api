package com.quickdeal.purchase.domain;

public record QuantityPerProduct(
    Long productId,
    Integer quantity
) {

}
