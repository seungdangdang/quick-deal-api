package com.quickdeal.order.service.domain;

public record QuantityPerProduct(
    Long productId,
    Integer quantity
) {

}
