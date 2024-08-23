package com.quickdeal.common.exception;

public class MaxUserLimitExceededException extends RuntimeException {

  public MaxUserLimitExceededException(String message) {
    super(message);
  }
}
