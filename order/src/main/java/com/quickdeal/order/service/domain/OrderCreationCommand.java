package com.quickdeal.order.service.domain;

import java.util.List;

public record OrderCreationCommand(
    String userUUID,
    List<QuantityPerProduct> quantityPerProducts
) {

}
