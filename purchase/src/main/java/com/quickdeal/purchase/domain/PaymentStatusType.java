package com.quickdeal.purchase.domain;

import lombok.Getter;

@Getter
public enum PaymentStatusType {
  PROCESSING,
  DONE,
  CANCEL,
  ERROR
}
