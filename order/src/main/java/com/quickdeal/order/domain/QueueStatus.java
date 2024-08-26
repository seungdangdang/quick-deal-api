package com.quickdeal.order.domain;

public record QueueStatus(
    boolean isSoldOut,
    boolean isExitedQueue,
    Long numberOfRemainingInQueue,
    String renewedQueueToken
) {

}
