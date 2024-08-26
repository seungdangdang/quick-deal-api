package com.quickdeal.order.domain;

import lombok.Getter;

@Getter
public enum PaymentStatusType {
  PROCESSING,
  DONE,
  CANCEL,
  ERROR
}
