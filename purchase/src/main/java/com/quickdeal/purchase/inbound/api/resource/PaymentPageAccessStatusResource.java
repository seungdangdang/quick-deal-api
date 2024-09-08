package com.quickdeal.purchase.inbound.api.resource;

import com.quickdeal.purchase.domain.PaymentPageAccessStatus;

public record PaymentPageAccessStatusResource(
    String status,
    Long numberOfRemainingInQueue,
    String renewedQueueToken,
    Long expiredAtEpochSeconds,
    long ticketNumber
) {

  public static PaymentPageAccessStatusResource from(
      PaymentPageAccessStatus status,
      Long expiredAtEpochSeconds,
      long ticketNumber) {
    return new PaymentPageAccessStatusResource(
        status.status().toString(),
        status.numberOfRemainingInQueue(),
        status.renewedQueueToken(),
        expiredAtEpochSeconds,
        ticketNumber
    );
  }
}
