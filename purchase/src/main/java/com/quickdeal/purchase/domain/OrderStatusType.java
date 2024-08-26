package com.quickdeal.purchase.domain;

import lombok.Getter;

@Getter
public enum OrderStatusType {
  PROCESSING,
  DONE,
  CANCEL,
  ERROR
}
