package com.quickdeal.order.api.controller;

import com.quickdeal.order.service.PaymentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

  private final PaymentService paymentService;

  public PaymentController(PaymentService checkoutService) {
    this.paymentService = checkoutService;
  }

  // 결제 요청
  @PutMapping("/checkout/{orderId}")
  public void checkout(@PathVariable Long orderId) {
    paymentService.processCheckout(orderId);
  }

  // 결제 취소
  @DeleteMapping("/checkout/{orderId}")
  public void cancelCheckout(@PathVariable Long orderId) {
    paymentService.cancelCheckout(orderId);
  }
}