package com.quickdeal.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({
    "com.quickdeal.common",
    "com.quickdeal.order",
    "com.quickdeal.product",
    "com.quickdeal.user"
})
@EnableJpaRepositories(basePackages = {
    "com.quickdeal.product.infrastructure.repository",
    "com.quickdeal.order.infrastructure.repository"
})
@EntityScan(basePackages = {
    "com.quickdeal.product.infrastructure.entity",
    "com.quickdeal.order.infrastructure.entity"
})
public class CoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(CoreApplication.class, args);
  }
}
