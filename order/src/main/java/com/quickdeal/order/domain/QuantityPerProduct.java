package com.quickdeal.order.domain;

public record QuantityPerProduct(
    Long productId,
    Integer quantity
) {

}
