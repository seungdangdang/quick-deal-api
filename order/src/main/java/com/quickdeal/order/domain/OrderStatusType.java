package com.quickdeal.order.domain;

import lombok.Getter;

@Getter
public enum OrderStatusType {
  PROCESSING,
  DONE,
  CANCEL,
  ERROR
}
