package com.quickdeal.order.service;

import com.quickdeal.order.domain.OrderStatusUpdateCommand;
import com.quickdeal.order.domain.PaymentCommand;
import com.quickdeal.order.infrastructure.entity.OrderStatus;
import com.quickdeal.order.infrastructure.entity.PaymentStatus;
import com.quickdeal.order.infrastructure.repository.PaymentRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderService orderService;

  public PaymentService(PaymentRepository paymentRepository,
      OrderService orderService) {
    this.paymentRepository = paymentRepository;
    this.orderService = orderService;
  }

  public void checkout(PaymentCommand paymentCommand) {

  }

  @Transactional
  public void endedCheckout(Long orderId) {
    orderService.updateOrderStatus(new OrderStatusUpdateCommand(orderId, OrderStatus.DONE));
    Instant paymentDate = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
    paymentRepository.updateOrderPayment(orderId, paymentDate, PaymentStatus.DONE);
    // todo - 상태값 반환 필요
  }

  @Transactional
  public void cancelCheckout(Long orderId) {
    orderService.updateOrderStatus(new OrderStatusUpdateCommand(orderId, OrderStatus.CANCEL));
    paymentRepository.updateOrderPayment(orderId, null, PaymentStatus.CANCEL);
    // todo - 상태값 반환 필요
  }

  @Transactional
  public void errorCheckout(Long orderId) {
    orderService.updateOrderStatus(new OrderStatusUpdateCommand(orderId, OrderStatus.ERROR));
    paymentRepository.updateOrderPayment(orderId, null, PaymentStatus.ERROR);
    // todo - 상태값 반환 필요
  }
}
