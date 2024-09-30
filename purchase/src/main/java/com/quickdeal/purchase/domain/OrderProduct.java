package com.quickdeal.purchase.domain;

public record OrderProduct(
    Long id,
    Long productId,
    Integer quantity,
    Integer price
) {

}
