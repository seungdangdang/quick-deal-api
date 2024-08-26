package com.quickdeal.order.api.resource;

import com.quickdeal.order.domain.QueueStatus;

public record QueueStatusResource(
    boolean isSoldOut,
    boolean isExitedQueue,
    Long numberOfRemainingInQueue,
    String renewedQueueToken
) {

  public static QueueStatusResource from(QueueStatus polling) {
    return new QueueStatusResource(
        polling.isSoldOut()
        , polling.isExitedQueue()
        , polling.numberOfRemainingInQueue()
        , polling.renewedQueueToken());
  }
}
