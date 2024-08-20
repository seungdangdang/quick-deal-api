package com.quickdeal.order.service.domain;

public record QueuePolling(
    boolean hasWaitEnded,
    Long numberOfRemainingInQueue,
    String newQueueToken
) {

}
