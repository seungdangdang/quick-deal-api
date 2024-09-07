package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.api.resource.PaymentStatusResource;
import com.quickdeal.purchase.domain.PaymentStatus;
import com.quickdeal.purchase.service.PurchaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

  private final Logger log;
  private final PurchaseService purchaseHandlerService;

  public PaymentController(PurchaseService orderHandlerService) {
    this.log = LoggerFactory.getLogger(PaymentController.class);
    this.purchaseHandlerService = orderHandlerService;
  }

  // :: 결제 요청 - 결제 진행하여 결제 상태를 반환받음
  @PostMapping("/products/{productId}/payment")
  public PaymentStatusResource payment(@PathVariable Long productId,
      @RequestBody PaymentRequestBody requestBody) {
    PaymentStatus result = purchaseHandlerService.paymentAndGetPaymentStatus(requestBody.orderId(), productId,
        requestBody.paymentAmount(), requestBody.userId());
    log.debug("<controller> [payment] finished payment, resultStatus: {}, orderId: {}",
        result.status(), result.orderId());
    return PaymentStatusResource.from(result);
  }
}
