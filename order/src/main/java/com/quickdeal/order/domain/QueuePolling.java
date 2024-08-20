package com.quickdeal.order.domain;

public record QueuePolling(
    boolean isExitedQueue,
    Long numberOfRemainingInQueue,
    String renewedQueueToken
) {

}
