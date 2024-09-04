package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.api.resource.PaymentPageAccessStatusResource;
import com.quickdeal.purchase.domain.PaymentPageAccessStatus;
import com.quickdeal.purchase.service.TicketService;
import com.quickdeal.purchase.service.TokenService;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueueController {

  private final long timeoutInSeconds;
  private final TicketService queueService;
  private final TokenService tokenService;
  private final Logger log;

  public QueueController(
      @Value("${payment.page.timeout-seconds}") long timeoutInSeconds,
      TicketService queueService, TokenService tokenService) {
    this.timeoutInSeconds = timeoutInSeconds;
    this.queueService = queueService;
    this.tokenService = tokenService;
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  @PostMapping("/queue")
  public PaymentPageAccessStatusResource validQueueStatus(
      @RequestParam String ticketToken) {
    Claims claims = tokenService.validateTokenAndGetClaims(ticketToken);
    PaymentPageAccessStatus queueStatus = queueService.getPaymentPageAccessStatusByTicket(
        ticketToken);
    log.debug(
        "<controller> [validQueueStatus] finished queue poll, orderId: {}, polling status: {}",
        claims.get("order_id"), queueStatus.status());

    long currentTimeInSeconds = Instant.now().getEpochSecond();
    long timeLimit = currentTimeInSeconds + timeoutInSeconds;
    return PaymentPageAccessStatusResource.from(queueStatus, timeLimit);
  }
}
