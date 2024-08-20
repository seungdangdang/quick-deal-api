package com.quickdeal.order.domain;

public record QueueStatus(
    boolean isExitedQueue,
    Long numberOfRemainingInQueue,
    String renewedQueueToken
) {

}
