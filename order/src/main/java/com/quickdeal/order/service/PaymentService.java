package com.quickdeal.order.service;

import com.quickdeal.common.service.ProductService;
import com.quickdeal.order.api.resource.CheckoutStatus;
import com.quickdeal.order.api.resource.CheckoutStatusResult;
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
  private final ProductService productService;

  public PaymentService(PaymentRepository paymentRepository,
      OrderService orderService, ProductService productService) {
    this.paymentRepository = paymentRepository;
    this.orderService = orderService;
    this.productService = productService;
  }

  @Transactional
  public CheckoutStatusResult checkout(PaymentCommand command) {
    if (productService.hasStockQuantityById(command.productId())) {
      //TODO: 결제 로직

      return new CheckoutStatusResult(CheckoutStatus.DONE_CHECKOUT, command.orderId(),
          command.paymentAmount());
    }

    return new CheckoutStatusResult(CheckoutStatus.ERROR, command.orderId(), null);
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
    productService.increaseStockQuantityById(orderId);
    // todo - 상태값 반환 필요
  }

  @Transactional
  public void errorCheckout(Long orderId) {
    orderService.updateOrderStatus(new OrderStatusUpdateCommand(orderId, OrderStatus.ERROR));
    paymentRepository.updateOrderPayment(orderId, null, PaymentStatus.ERROR);
    productService.increaseStockQuantityById(orderId);
    // todo - 상태값 반환 필요
  }
}
