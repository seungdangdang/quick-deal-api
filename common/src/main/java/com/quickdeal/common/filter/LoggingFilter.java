package com.quickdeal.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

  @Override
  public void init(FilterConfig filterConfig) {
    // 필터 초기화 시 로직
    log.info("Initializing LoggingFilter...");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // 요청 로그 기록
    log.info("[REQ] {} {}", httpRequest.getMethod(),
        httpRequest.getRequestURI());

    // 필터 체인 실행
    chain.doFilter(request, response);

    // 응답 상태 코드에 따라 로그 레벨 설정
    int status = httpResponse.getStatus();
    log.debug("[RES] {} {} {}", httpRequest.getMethod(),
        httpRequest.getRequestURI(), status);
  }

  @Override
  public void destroy() {
    log.info("Destroying LoggingFilter...");
  }
}
