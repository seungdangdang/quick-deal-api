package com.quickdeal.order.api.resource;

import com.quickdeal.order.domain.PaymentPageAccessStatus;

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
