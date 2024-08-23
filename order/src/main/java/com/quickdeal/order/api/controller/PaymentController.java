package com.quickdeal.order.api.controller;

import com.quickdeal.order.api.resource.PaymentRequestBody;
import com.quickdeal.order.service.OrderHandlerService;
import com.quickdeal.order.service.PaymentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

  private final PaymentService paymentService;
  private final OrderHandlerService orderHandlerService;

  public PaymentController(PaymentService checkoutService,
      OrderHandlerService orderHandlerService) {
    this.paymentService = checkoutService;
    this.orderHandlerService = orderHandlerService;
  }

  // 결제 요청
  @PutMapping("/checkout")
  public void checkout(@RequestBody PaymentRequestBody requestBody) {
    orderHandlerService.processCheckout(requestBody.toCommand());
  }

  // 결제 취소
  @DeleteMapping("/checkout/{orderId}")
  public void cancelCheckout(@PathVariable Long orderId) {
    paymentService.cancelCheckout(orderId);
  }
}