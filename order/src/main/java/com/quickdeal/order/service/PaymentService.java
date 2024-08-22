package com.quickdeal.order.service;

import com.quickdeal.order.domain.OrderStatusUpdateCommand;
import com.quickdeal.order.infrastructure.entity.OrderStatus;
import com.quickdeal.order.infrastructure.entity.PaymentStatus;
import com.quickdeal.order.infrastructure.repository.PaymentRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderService orderService;

  public PaymentService(PaymentRepository paymentRepository,
      OrderService orderService) {
    this.paymentRepository = paymentRepository;
    this.orderService = orderService;
  }

  public void processCheckout(Long orderId) {
    // todo - 실제 결제 로직 추가

    // 결제가 완료 여부에 따라 상태업데이트
    updateDoneOrderPayment(orderId);
    // todo - 상태값 반환 필요
  }

  private void updateDoneOrderPayment(Long orderId) {
    // 주문 상태 업데이트 하기
    orderService.updateOrderStatus(new OrderStatusUpdateCommand(orderId, OrderStatus.DONE));
    // 결제 상태 업데이트 하기
    paymentRepository.updateOrderPayment(orderId, LocalDateTime.now(), PaymentStatus.DONE);
    // todo - 상태값 반환 필요
  }

  public void cancelCheckout(Long orderId) {
    orderService.updateOrderStatus(new OrderStatusUpdateCommand(orderId, OrderStatus.CANCEL));
    paymentRepository.updateOrderPayment(orderId, null, PaymentStatus.CANCEL);
    // todo - 상태값 반환 필요
  }
}
