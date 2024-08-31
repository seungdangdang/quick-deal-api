package com.quickdeal.purchase.domain;

public record PaymentPageAccessStatus(
    PageAccessStatuses status,
    Long numberOfRemainingInQueue,
    String renewedQueueToken
) {

}
