package com.quickdeal.common.filter;

import io.micrometer.common.lang.NonNull;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiRequestMetricsInterceptor implements HandlerInterceptor {

  private final MeterRegistry meterRegistry;
  private final Logger log;

  public ApiRequestMetricsInterceptor(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    this.log = LoggerFactory.getLogger(ApiRequestMetricsInterceptor.class);
  }

  @Override
  public void afterCompletion(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler, Exception ex
  ) {
    try {
      String path = request.getRequestURI();
      String method = request.getMethod();
      String status = Integer.toString(response.getStatus());

      Counter counter = meterRegistry.counter(
          "api_requests_total",
          "callee", "[" + method + "] " + path,
          "status", status
      );
      counter.increment();
    } catch (Exception e) {
      log.error("[afterCompletion] Failed to record API metrics", e);
    }
  }
}
