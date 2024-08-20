package com.quickdeal.order.api.controller;

import com.quickdeal.order.api.resource.QueuePollingRequestBody;
import com.quickdeal.order.api.resource.QueueStatusResource;
import com.quickdeal.order.api.resource.QueueRequestBody;
import com.quickdeal.order.api.resource.QueueTokenResource;
import com.quickdeal.order.service.OrderHandlerService;
import com.quickdeal.order.service.QueueService;
import com.quickdeal.order.domain.QueueStatus;
import com.quickdeal.order.domain.QueueToken;
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

  @PostMapping("/queue")
  public QueueTokenResource generateQueueNumber(@RequestBody QueueRequestBody requestBody) {
    QueueToken token = apiService.generateQueue(requestBody.toCommand());
    return QueueTokenResource.from(token);
  }

  @PostMapping("/queue/polling")
  public QueueStatusResource validQueueStatus(@RequestBody QueuePollingRequestBody requestBody) {
    QueueStatus queueStatus = queueService.getUpdatedQueueStatus(requestBody.toCommand());
    return QueueStatusResource.from(queueStatus);
  }
}
