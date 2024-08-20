package com.quickdeal.order.infrastructure.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
  PROCESSING,
  DONE,
  CANCEL;
}
