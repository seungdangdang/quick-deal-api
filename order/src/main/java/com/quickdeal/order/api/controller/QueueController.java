package com.quickdeal.order.api.controller;

import com.quickdeal.order.api.resource.QueuePollingResource;
import com.quickdeal.order.api.resource.QueuePollingRequestBody;
import com.quickdeal.order.api.resource.QueuePollingResultResource;
import com.quickdeal.order.api.resource.QueueResource;
import com.quickdeal.order.api.resource.QueueTokenResource;
import com.quickdeal.order.service.OrderHandlerService;
import com.quickdeal.order.service.QueueService;
import com.quickdeal.order.service.domain.QueuePolling;
import com.quickdeal.order.service.domain.QueueToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueueController {

  private final OrderHandlerService apiService;
  private final QueueService queueService;

  public QueueController(OrderHandlerService apiService, QueueService queueService) {
    this.apiService = apiService;
    this.queueService = queueService;
  }

  @PostMapping("/queue/{userId}")
  public QueueResource generateQueueNumber(@PathVariable("userId") String userId) {
    QueueToken token = apiService.generateQueue(userId);
    QueueTokenResource resource = QueueTokenResource.from(token);

    return new QueueResource(resource);
  }

  @PostMapping("/queue/polling")
  public QueuePollingResultResource validQueueStatus(@RequestBody QueuePollingRequestBody requestBody) {
    QueuePolling queuePolling = queueService.checkQueueStatus(requestBody.toCommand());
    QueuePollingResource resource = QueuePollingResource.from(queuePolling);

    return new QueuePollingResultResource(resource);
  }
}
