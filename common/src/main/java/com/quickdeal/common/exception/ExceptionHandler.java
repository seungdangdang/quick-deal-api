package com.quickdeal.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandler {

  private final Logger log;

  public ExceptionHandler() {
    this.log = LoggerFactory.getLogger(ExceptionHandler.class);
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponseBody> handleNotFoundException(NotFoundException exception, HttpServletRequest request) {
    log.warn("[handleNotFoundException] exception: {}", exception.getMessage());
    ErrorResponseBody body = new ErrorResponseBody (
        Instant.now(),
        HttpStatus.NOT_FOUND.value(),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        exception.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity.badRequest().body(body);
  }
}
