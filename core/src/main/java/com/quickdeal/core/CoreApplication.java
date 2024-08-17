package com.quickdeal.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
    "com.quickdeal.common",
    "com.quickdeal.order",
    "com.quickdeal.payment",
    "com.quickdeal.product",
    "com.quickdeal.user",
    "com.quickdeal.core.config"
})
public class CoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(CoreApplication.class, args);
  }
}
