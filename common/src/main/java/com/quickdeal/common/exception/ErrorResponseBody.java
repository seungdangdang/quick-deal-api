package com.quickdeal.common.exception;

import java.time.Instant;

public record ErrorResponseBody(
    Instant timestamp,
    Integer status,
    String error,
    String message,
    String path
) {}
