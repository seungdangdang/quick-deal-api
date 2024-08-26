package com.quickdeal.order.api.controller;

import com.quickdeal.order.api.resource.CheckoutStatusResult;
import com.quickdeal.order.api.resource.PaymentRequestBody;
import com.quickdeal.order.service.OrderHandlerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

  private final OrderHandlerService orderHandlerService;

  public PaymentController(OrderHandlerService orderHandlerService) {
    this.orderHandlerService = orderHandlerService;
  }

  // 결제 요청
  @PutMapping("/checkout")
  public void checkout(@RequestBody PaymentRequestBody requestBody) {
    CheckoutStatusResult result = orderHandlerService.processCheckout(requestBody.toCommand());

    //TODO- 반환타입 레코드 만들어서 반환하기
  }

  // 결제 취소
  @DeleteMapping("/checkout/{orderId}")
  public void cancelCheckout(@PathVariable Long orderId) {
    orderHandlerService.cancelCheckout(orderId);

    //TODO- 반환타입 레코드 만들어서 반환하기
  }
}