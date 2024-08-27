package com.quickdeal.purchase.domain;

public record OrderProductInfo(
    Long id,
    String name,
    Integer quantity,
    Integer price
) {

}
