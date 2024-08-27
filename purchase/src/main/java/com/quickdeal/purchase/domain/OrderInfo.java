package com.quickdeal.purchase.domain;

public record OrderInfo(
    Long id,
    OrderProductInfo products
) {

}
