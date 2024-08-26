package com.quickdeal.purchase.domain;

public record PaymentPageAccessStatus(
    PageAccessStatusType status,
    Long numberOfRemainingInQueue,
    String renewedQueueToken
) {

}
