package com.quickdeal.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.quickdeal.common")
@ComponentScan("com.quickdeal.order")
@ComponentScan("com.quickdeal.payment")
@ComponentScan("com.quickdeal.product")
@ComponentScan("com.quickdeal.user")
public class CoreApplication {

  public static void main(String[] args) {

  }
}
