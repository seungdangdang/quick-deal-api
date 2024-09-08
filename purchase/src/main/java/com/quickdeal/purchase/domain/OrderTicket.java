package com.quickdeal.purchase.domain;

public record OrderTicket(
    String stringToken,
    Long productId,
    String userId,
    Long number
) {

}
