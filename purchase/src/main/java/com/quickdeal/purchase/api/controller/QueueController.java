package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.api.resource.PaymentPageAccessStatusResource;
import com.quickdeal.purchase.domain.PaymentPageAccessStatus;
import com.quickdeal.purchase.service.TicketService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueueController {

  private final TicketService queueService;

  public QueueController(TicketService queueService) {
    this.queueService = queueService;
  }

  @PostMapping("/queue")
  public PaymentPageAccessStatusResource validQueueStatus(
      @RequestParam String ticketToken) {
    PaymentPageAccessStatus queueStatus = queueService.getPaymentPageAccessStatusByTicket(
        ticketToken);
    return PaymentPageAccessStatusResource.from(queueStatus);
  }
}
