package com.quickdeal.order.api.controller;

import com.quickdeal.order.api.resource.OrderCreateRequestBody;
import com.quickdeal.order.api.resource.OrderResource;
import com.quickdeal.order.service.OrderService;
import com.quickdeal.order.domain.Order;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping("/orders")
  public OrderResource createOrder(@RequestBody OrderCreateRequestBody requestBody) {
    Order order = orderService.createOrder(requestBody.toCommand());
    return OrderResource.from(order);
  }
}
