package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.api.resource.PaymentPageResource;
import com.quickdeal.purchase.api.resource.PaymentStatusResource;
import com.quickdeal.purchase.domain.OrderInfo;
import com.quickdeal.purchase.domain.PaymentStatus;
import com.quickdeal.purchase.service.OrderService;
import com.quickdeal.purchase.service.PurchaseHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

  private final Logger log;
  private final long timeoutInSeconds;
  private final PurchaseHandlerService purchaseHandlerService;
  private final OrderService orderService;

  public PaymentController(@Value("${payment.page.timeout-seconds}") long timeoutInSeconds,
      PurchaseHandlerService orderHandlerService, OrderService orderService) {
    this.orderService = orderService;
    this.log = LoggerFactory.getLogger(PaymentController.class);
    this.timeoutInSeconds = timeoutInSeconds;
    this.purchaseHandlerService = orderHandlerService;
  }

  // :: 결제정보 조회
  @GetMapping("/products/{productId}/payment")
  public PaymentPageResource getPaymentPageResource(@PathVariable Long productId,
      @RequestParam("orderId") Long orderId) {
    OrderInfo orderInfo = orderService.getOrderInfo(orderId);
    return PaymentPageResource.from(orderInfo, timeoutInSeconds);
  }

  // :: 결제 요청 - 결제 진행하여 결제 상태를 반환받음
  @PostMapping("/products/{productId}/payment")
  public PaymentStatusResource payment(@PathVariable Long productId,
      @RequestBody PaymentRequestBody requestBody) {
    PaymentStatus result = purchaseHandlerService.payment(requestBody.orderId(), productId,
        requestBody.paymentAmount(), requestBody.userId());
    log.debug("<controller> [payment] finished payment, resultStatus: {}, orderId: {}",
        result.status(), result.orderId());
    return PaymentStatusResource.from(result);
  }
}
