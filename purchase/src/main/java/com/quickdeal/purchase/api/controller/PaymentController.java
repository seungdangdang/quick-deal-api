package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.api.resource.PaymentRequestBody;
import com.quickdeal.purchase.domain.CheckoutStatus;
import com.quickdeal.purchase.api.resource.CheckoutStatusResource;
import com.quickdeal.purchase.service.PurchaseHandlerService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

  private final PurchaseHandlerService purchaseHandlerService;

  public PaymentController(PurchaseHandlerService orderHandlerService) {
    this.purchaseHandlerService = orderHandlerService;
  }

  // :: 결제 요청 - 결제 진행하여 결제 상태를 반환받음
  @PutMapping("/checkout")
  public CheckoutStatusResource checkout(@RequestBody PaymentRequestBody requestBody) {
    CheckoutStatus result = purchaseHandlerService.handleCheckoutProcess(requestBody.toCommand());
    return CheckoutStatusResource.from(result);
  }
}