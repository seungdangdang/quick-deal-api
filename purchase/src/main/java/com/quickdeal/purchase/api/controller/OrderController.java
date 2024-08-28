package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.api.resource.OrderCreateRequestBody;
import com.quickdeal.purchase.api.resource.QueueTokenResource;
import com.quickdeal.purchase.domain.OrderInfo;
import com.quickdeal.purchase.domain.OrderResource;
import com.quickdeal.purchase.domain.Ticket;
import com.quickdeal.purchase.service.OrderService;
import com.quickdeal.purchase.service.PurchaseHandlerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

  private final PurchaseHandlerService purchaseHandlerService;
  private final OrderService orderService;

  public OrderController(PurchaseHandlerService orderHandlerService, OrderService orderService) {
    this.purchaseHandlerService = orderHandlerService;
    this.orderService = orderService;
  }

  // :: 주문 생성 (주문정보 저장 / 주문상품정보 저장 / 결제정보 저장 / 대기열토큰 발급 및 반환)
  // :: 유저의 주문 요청 시 - 주문 관련 정보를 생성하고, 대기열에 요청을 큐잉한다
  // Q: 반환타입이 뭔가 맘에 안듦 'order' 를 내포한 것을 반환해야 하지 않을까?
  // Q: 메서드명도 맘에 안듦
  @PostMapping("/orders")
  public QueueTokenResource createOrderAndProcessQueue(
      @RequestBody OrderCreateRequestBody requestBody) {
    Ticket token = purchaseHandlerService.getTicket(requestBody.toCommand());
    return QueueTokenResource.from(token);
  }

  // :: 주문 취소
  @DeleteMapping("/orders/{orderId}")
  public OrderResource cancelCheckout(@PathVariable Long orderId) {
    OrderInfo orderInfo = purchaseHandlerService.handleCancelCheckout(orderId);
    return OrderResource.from(orderInfo);
  }

  // :: 주문 상세 정보
  @PostMapping("/orders/{orderId}")
  public OrderResource getOrderDetail(@PathVariable Long orderId) {
    OrderInfo orderInfo = orderService.getOrderInfo(orderId);
    return OrderResource.from(orderInfo);
  }
}
