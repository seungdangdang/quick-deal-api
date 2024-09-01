package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.api.resource.QueueTokenResource;
import com.quickdeal.purchase.domain.OrderInfo;
import com.quickdeal.purchase.domain.OrderResource;
import com.quickdeal.purchase.domain.Ticket;
import com.quickdeal.purchase.service.OrderService;
import com.quickdeal.purchase.service.PurchaseHandlerService;
import com.quickdeal.purchase.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

  private final PurchaseHandlerService purchaseHandlerService;
  private final OrderService orderService;
  private final TokenService tokenService;
  private final Logger log;

  public OrderController(PurchaseHandlerService orderHandlerService, OrderService orderService,
      TokenService tokenService) {
    this.purchaseHandlerService = orderHandlerService;
    this.orderService = orderService;
    this.tokenService = tokenService;
    this.log = LoggerFactory.getLogger(OrderController.class);
  }

  // :: 주문 생성 (주문정보 저장 / 주문상품정보 저장 / 결제정보 저장 / 대기열토큰 발급 및 반환)
  // :: 유저의 주문 요청 시 - 주문 관련 정보를 생성하고, 대기열에 요청을 큐잉한다
  @PostMapping("/orders")
  public QueueTokenResource createOrderAndProcessQueue(
      @RequestBody OrderCreateRequestBody requestBody) {
    log.debug("<controller> [orders] start orders, UUID : {}", requestBody.userUUID());
    Ticket token = purchaseHandlerService.getTicket(requestBody.toCommand());
    return QueueTokenResource.from(token);
  }

  // :: 주문 취소
  @DeleteMapping("/orders/{orderId}")
  public OrderResource cancelPayment(@PathVariable Long orderId, @RequestParam String userId) {
    orderService.validateAvailableOrder(orderId);
    OrderInfo orderInfo = purchaseHandlerService.handleCancelPayment(orderId, userId);
    return OrderResource.from(orderInfo);
  }

  // :: 주문 상세 정보
  @GetMapping("/orders/{orderId}")
  public OrderResource getOrderDetail(@PathVariable Long orderId) {
    OrderInfo orderInfo = orderService.getOrderInfo(orderId);
    return OrderResource.from(orderInfo);
  }
}
