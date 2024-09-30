package com.quickdeal.common.exception;

public class OrderTimeExpiredException extends RuntimeException {

  public OrderTimeExpiredException(String message) {
    super(message);
  }
}
