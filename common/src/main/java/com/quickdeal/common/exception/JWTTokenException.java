package com.quickdeal.common.exception;

public class JWTTokenException extends RuntimeException {

  public JWTTokenException(String message) {
    super(message);
  }
}
