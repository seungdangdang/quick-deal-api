package com.quickdeal.order.service.domain;

public record QueuePolling(
    boolean isExitedQueue,
    Long numberOfRemainingInQueue,
    String renewedQueueToken
) {

}
