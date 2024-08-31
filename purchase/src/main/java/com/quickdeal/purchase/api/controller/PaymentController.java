package com.quickdeal.purchase.api.controller;

import com.quickdeal.purchase.api.resource.PaymentStatusResource;
import com.quickdeal.purchase.domain.PaymentStatus;
import com.quickdeal.purchase.service.PurchaseHandlerService;
import com.quickdeal.purchase.service.TokenService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

  private final Logger log;
  private final PurchaseHandlerService purchaseHandlerService;
  private final TokenService tokenService;

  public PaymentController(PurchaseHandlerService orderHandlerService, TokenService tokenService) {
    this.log = LoggerFactory.getLogger(PaymentController.class);
    this.purchaseHandlerService = orderHandlerService;
    this.tokenService = tokenService;
  }

  // :: 결제 요청 - 결제 진행하여 결제 상태를 반환받음
// TODO: @PutMapping("/products/{productId}/checkout") <- 이런식으로? URL 수정할 것
  @PutMapping("/payment")
  public PaymentStatusResource payment(@RequestBody PaymentRequestBody requestBody) {
    Claims claims = tokenService.validateTokenAndGetClaims(requestBody.ticketToken());
    String userID = claims.get("user_id", String.class);
    Long orderId = claims.get("order_id", Long.class);
    Long productId = claims.get("product_id", Long.class);
    PaymentStatus result = purchaseHandlerService.payment(
        orderId,
        productId,
        requestBody.paymentAmount(),
        userID
    );
    log.debug("<controller> [payment] finished payment, resultStatus: {}, orderId: {}",
        result.status(),
        result.orderId());
    return PaymentStatusResource.from(result);
  }
}
