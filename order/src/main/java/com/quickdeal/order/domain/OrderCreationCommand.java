package com.quickdeal.order.domain;

import java.util.List;

public record OrderCreationCommand(
    String userUUID,
    List<QuantityPerProduct> quantityPerProducts
) {

}
