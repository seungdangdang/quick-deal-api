package com.quickdeal.order.infrastructure.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
  PROCESSING,
  DONE,
  CANCEL,
  ERROR
}
