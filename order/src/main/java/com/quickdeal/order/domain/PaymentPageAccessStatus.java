package com.quickdeal.order.domain;

public record PaymentPageAccessStatus(
    PageAccessStatusType status,
    Long numberOfRemainingInQueue,
    String renewedQueueToken
) {

}
