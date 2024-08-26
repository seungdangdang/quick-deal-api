package com.quickdeal.order.api.resource;

public record QueueEntryRequestParams(
    Long productId,
    Long queueNumber
) {

}
