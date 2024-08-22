package com.quickdeal.order.domain;

public record OrderCreationCommand(
    String userUUID,
    QuantityPerProduct quantityPerProduct
) {

}
