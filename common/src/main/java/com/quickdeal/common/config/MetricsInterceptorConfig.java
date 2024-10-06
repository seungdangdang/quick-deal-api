package com.quickdeal.common.config;

import com.quickdeal.common.filter.ApiRequestMetricsInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MetricsInterceptorConfig implements WebMvcConfigurer {

  private final ApiRequestMetricsInterceptor apiRequestMetricsInterceptor;

  public MetricsInterceptorConfig(ApiRequestMetricsInterceptor apiRequestMetricsInterceptor) {
    this.apiRequestMetricsInterceptor = apiRequestMetricsInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(apiRequestMetricsInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/error"); // 에러 발생 시 자동으로 /error 경로를 라우팅하기 때문에 매트릭이 이중으로 기록됨을 방지하기 위해 제거
  }
}
