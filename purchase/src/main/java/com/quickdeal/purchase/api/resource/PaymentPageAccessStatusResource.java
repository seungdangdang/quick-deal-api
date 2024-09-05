package com.quickdeal.purchase.api.resource;

import com.quickdeal.purchase.domain.PaymentPageAccessStatus;

public record PaymentPageAccessStatusResource(
    String status,
    Long numberOfRemainingInQueue,
    String renewedQueueToken,
    long expiredAtEpochSeconds,
    long ticketNumber
) {

  public static PaymentPageAccessStatusResource from(
      PaymentPageAccessStatus status,
      long timeLimit,
      long ticketNumber) {
    return new PaymentPageAccessStatusResource(
        status.status().toString(),
        status.numberOfRemainingInQueue(),
        status.renewedQueueToken(),
        timeLimit,
        ticketNumber
    );
  }
}
