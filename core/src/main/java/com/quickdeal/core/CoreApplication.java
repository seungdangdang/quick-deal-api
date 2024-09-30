package com.quickdeal.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@ComponentScan({
    "com.quickdeal.common",
    "com.quickdeal.purchase",
    "com.quickdeal.product",
    "com.quickdeal.user",
    "com.quickdeal.auth",
    "com.quickdeal.scheduler"
})
@EnableJpaRepositories(basePackages = {
    "com.quickdeal.product.infrastructure.repository",
    "com.quickdeal.purchase.outbound.rdb.repository",
    "com.quickdeal.purchase.outbound.redis.repository",
})
@EntityScan(basePackages = {
    "com.quickdeal.product.infrastructure.entity",
    "com.quickdeal.purchase.outbound.rdb.model"
})
public class CoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(CoreApplication.class, args);
  }

  @Bean
  public WebMvcConfigurer CORSConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
      }
    };
  }
}
