package com.quickdeal.purchase.domain;

import com.quickdeal.common.exception.BusinessRuleViolation;

public record OrderCreationCommand(
    String userUUID,
    QuantityPerProduct quantityPerProduct
) {

  public OrderCreationCommand {
    if (quantityPerProduct.quantity() > 1) {
      throw new BusinessRuleViolation("주문 1회 당 최대 주문 가능 수량인 1개를 초과했습니다.");
    }
  }
}
