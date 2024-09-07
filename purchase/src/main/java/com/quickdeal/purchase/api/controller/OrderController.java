package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.api.resource.TicketTokenResource;
import com.quickdeal.purchase.domain.OrderInfo;
import com.quickdeal.purchase.domain.OrderResource;
import com.quickdeal.purchase.domain.Ticket;
import com.quickdeal.purchase.service.OrderService;
import com.quickdeal.purchase.service.PurchaseHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

  private final PurchaseHandlerService purchaseHandlerService;
  private final OrderService orderService;
  private final Logger log;

  public OrderController(PurchaseHandlerService orderHandlerService, OrderService orderService) {
    this.purchaseHandlerService = orderHandlerService;
    this.orderService = orderService;
    this.log = LoggerFactory.getLogger(OrderController.class);
  }

  // :: 주문 생성 (주문정보 저장 / 주문상품정보 저장 / 결제정보 저장 / 대기열토큰 발급 및 반환)
  // :: 유저의 주문 요청 시 - 주문 관련 정보를 생성하고, 대기열에 요청을 큐잉한다
  @PostMapping("/orders")
  public TicketTokenResource createOrderAndProcessQueue(
      @RequestBody OrderCreateRequestBody requestBody) {
    log.debug("<controller> [orders] start orders, UUID : {}", requestBody.userId());
    Ticket token = purchaseHandlerService.getTicket(requestBody.toCommand());
    return TicketTokenResource.from(token);
  }

  // :: 주문 취소
  @DeleteMapping("/orders/{orderId}")
  public void cancelPayment(@PathVariable Long orderId, @RequestBody OrderCancelRequestBody requestBody) {
    log.debug("<controller> [orders] start cancel payment, UUID : {}", requestBody.userId());
    orderService.validateAvailableOrder(orderId);
    purchaseHandlerService.updatePaymentCancellationStatus(orderId, requestBody.productId(), requestBody.userId());
  }

  // :: 주문 상세 정보
  @GetMapping("/orders/{orderId}")
  public OrderResource getOrderDetail(@PathVariable Long orderId) {
    OrderInfo orderInfo = orderService.getOrderInfo(orderId);
    return OrderResource.from(orderInfo);
  }
}
