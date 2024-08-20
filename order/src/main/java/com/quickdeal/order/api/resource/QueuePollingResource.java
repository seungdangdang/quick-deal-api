package com.quickdeal.order.api.resource;

import com.quickdeal.order.domain.QueuePolling;

public record QueuePollingResource(
    boolean isExitedQueue,
    Long numberOfRemainingInQueue,
    String renewedQueueToken
) {

  public static QueuePollingResource from(QueuePolling polling) {
    return new QueuePollingResource(polling.isExitedQueue(), polling.numberOfRemainingInQueue(),
        polling.renewedQueueToken());
  }
}
