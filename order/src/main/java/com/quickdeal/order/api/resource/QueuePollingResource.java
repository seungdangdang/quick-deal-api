package com.quickdeal.order.api.resource;

import com.quickdeal.order.service.domain.QueuePolling;

public record QueuePollingResource(
    boolean endWait,
    Long remainingInQueue,
    String newQueueToken
) {

  public static QueuePollingResource from(QueuePolling polling) {
    return new QueuePollingResource(polling.hasWaitEnded(), polling.numberOfRemainingInQueue(),
        polling.newQueueToken());
  }
}
