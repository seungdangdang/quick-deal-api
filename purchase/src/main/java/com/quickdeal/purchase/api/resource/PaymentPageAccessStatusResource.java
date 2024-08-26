package com.quickdeal.purchase.api.resource;

import com.quickdeal.purchase.domain.PaymentPageAccessStatus;

public record PaymentPageAccessStatusResource(
    String status,
    Long numberOfRemainingInQueue,
    String renewedQueueToken
) {

  public static PaymentPageAccessStatusResource from(PaymentPageAccessStatus status) {
    return new PaymentPageAccessStatusResource(
        status.status().toString(), status.numberOfRemainingInQueue(),
        status.renewedQueueToken());
  }
}
