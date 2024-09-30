package com.quickdeal.common.exception;

public class OrderStatusInvalidException extends RuntimeException {

  public OrderStatusInvalidException(String message) {
    super(message);
  }
}
