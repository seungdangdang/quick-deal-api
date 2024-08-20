package com.quickdeal.order.api.resource;

public record QuantityPerProductRequest(
    Long productId,
    Integer quantity
) {

}
