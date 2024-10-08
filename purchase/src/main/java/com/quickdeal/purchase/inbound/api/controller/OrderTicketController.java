package com.quickdeal.purchase.inbound.api.controller;

import com.quickdeal.purchase.domain.OrderTicket;
import com.quickdeal.purchase.domain.PageAccessStatuses;
import com.quickdeal.purchase.domain.PaymentPageAccessStatus;
import com.quickdeal.purchase.inbound.api.resource.OrderTicketResource;
import com.quickdeal.purchase.inbound.api.resource.PaymentPageAccessStatusResource;
import com.quickdeal.purchase.service.OrderTicketService;
import com.quickdeal.purchase.service.OrderTicketTokenService;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderTicketController {

  private final Duration timeoutInSeconds;
  private final Logger log;
  private final OrderTicketService ticketService;
  private final OrderTicketTokenService orderTicketTokenService;

  public OrderTicketController(
      @Value("${order-creation.timeout-seconds}") Duration timeoutInSeconds,
      OrderTicketService ticketService,
      OrderTicketTokenService orderTicketTokenService) {
    this.timeoutInSeconds = timeoutInSeconds;
    this.log = LoggerFactory.getLogger(OrderTicketController.class);
    this.ticketService = ticketService;
    this.orderTicketTokenService = orderTicketTokenService;
  }

  @PostMapping("/orders/ticket")
  public OrderTicketResource createTicket(@RequestBody CreateTicketRequestBody requestBody) {
    log.debug("[POST][/orders/ticket] request body: {}", requestBody);
    OrderTicket ticket = ticketService.issueOrderTicket(requestBody.userId(),
        requestBody.productId());

    return OrderTicketResource.from(ticket);
  }

  @GetMapping("/orders/queue/status")
  public ResponseEntity<PaymentPageAccessStatusResource> getPaymentPageAccessStatus(
      @RequestParam String ticket
  ) {
    Claims claims = orderTicketTokenService.validateTokenAndGetClaims(ticket);
    log.debug("[GET][/orders/queue/status] request with jwt. claims: {}", claims);
    PaymentPageAccessStatus queueStatus = ticketService.getPaymentPageAccessStatus(ticket);

    Long expiredAtEpochSeconds = null;
    if (queueStatus.status() == PageAccessStatuses.ACCESS_GRANTED) {
      expiredAtEpochSeconds = timeoutInSeconds.getSeconds() + Instant.now().getEpochSecond();
    }

    log.debug(
        "[GET][/orders/queue/status] finished queue poll, polling status: {}, claims: {}",
        queueStatus.status(),
        claims
    );
    PaymentPageAccessStatusResource resource = PaymentPageAccessStatusResource.from(
        queueStatus,
        expiredAtEpochSeconds,
        claims.get("ticket_number", Long.class)
    );

    HttpStatus status = switch (queueStatus.status()) {
      case ACCESS_GRANTED -> HttpStatus.OK;
      case ACCESS_DENIED -> HttpStatus.FORBIDDEN;
      case ITEM_SOLD_OUT -> HttpStatus.CONFLICT;
    };

    return new ResponseEntity<>(resource, status);
  }
}
