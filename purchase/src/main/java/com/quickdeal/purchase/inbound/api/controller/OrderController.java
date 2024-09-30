package com.quickdeal.purchase.inbound.api.controller;

import com.quickdeal.purchase.domain.Order;
import com.quickdeal.purchase.domain.OrderInfo;
import com.quickdeal.purchase.domain.OrderResource;
import com.quickdeal.purchase.service.OrderService;
import com.quickdeal.purchase.service.PurchaseService;
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

  private final PurchaseService purchaseHandlerService;
  private final OrderService orderService;
  private final Logger log;

  public OrderController(PurchaseService orderHandlerService, OrderService orderService) {
    this.purchaseHandlerService = orderHandlerService;
    this.orderService = orderService;
    this.log = LoggerFactory.getLogger(OrderController.class);
  }

  // :: 주문 생성 (주문정보 저장 / 주문상품정보 저장 / 결제정보 저장)
  @PostMapping("/orders")
  public long createOrder(@RequestBody OrderCreateRequestBody requestBody) {
    log.debug("[POST][/orders] requestBody: {}", requestBody);
    Order order = purchaseHandlerService.saveOrder(requestBody.toCommand());
    return order.id();
  }

  // :: 주문 취소
  @DeleteMapping("/orders/{orderId}")
  public void cancelOrder(@PathVariable Long orderId,
      @RequestBody OrderCancelRequestBody requestBody) {
    log.debug("<controller> [orders] start cancel payment, UUID : {}", requestBody.userId());
    orderService.validateAvailableOrder(orderId);
    purchaseHandlerService.saveCancel(orderId, requestBody.productId(),
        requestBody.userId());
  }

  // :: 주문 상세 정보
  @GetMapping("/orders/{orderId}")
  public OrderResource getOrderDetail(@PathVariable Long orderId) {
    OrderInfo orderInfo = orderService.getOrderDetailInfo(orderId);
    return OrderResource.from(orderInfo);
  }
}
