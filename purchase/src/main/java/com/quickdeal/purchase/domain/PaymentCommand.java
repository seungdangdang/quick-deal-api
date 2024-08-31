package com.quickdeal.purchase.domain;

public record PaymentCommand(
    String ticketToken,
    int paymentAmount
) {

}
