package com.quickdeal.order.service;

import com.quickdeal.common.exception.BusinessRuleViolation;
import com.quickdeal.common.service.ProductService;
import com.quickdeal.order.domain.Order;
import com.quickdeal.order.domain.OrderCreationCommand;
import com.quickdeal.order.infrastructure.entity.OrderEntity;
import com.quickdeal.order.infrastructure.entity.OrderProductEntity;
import com.quickdeal.order.infrastructure.entity.PaymentEntity;
import com.quickdeal.order.infrastructure.repository.OrderProductRepository;
import com.quickdeal.order.infrastructure.repository.OrderRepository;
import com.quickdeal.order.infrastructure.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderProductRepository orderProductRepository;
  private final PaymentRepository paymentRepository;
  private final ProductService productService;

  public OrderService(OrderRepository orderRepository,
      OrderProductRepository orderProductRepository,
      PaymentRepository paymentRepository, ProductService productService) {
    this.orderRepository = orderRepository;
    this.orderProductRepository = orderProductRepository;
    this.paymentRepository = paymentRepository;
    this.productService = productService;
  }

  @Transactional
  public Order createOrder(OrderCreationCommand command) {
    validationOrder(command);

    OrderEntity orderEntity = OrderEntity.createOrder(command.userUUID());

    // 주문 저장
    OrderEntity savedOrder = orderRepository.save(orderEntity);

    // 주문-상품 저장
    Long productId = command.quantityPerProduct().productId();
    int price = productService.getPriceById(productId);

    OrderProductEntity orderProductEntity = OrderProductEntity.createOrderProduct(savedOrder,
        productId, command.quantityPerProduct().quantity(), price);
    orderProductRepository.save(orderProductEntity);

    // 결제 정보 저장
    OrderProductEntity orderProducts = orderProductRepository.findByOrderId(
        savedOrder.getId());

    int totalAmount = orderProducts.getPrice() * orderProducts.getQuantity();

    PaymentEntity paymentEntity = PaymentEntity.createPayment(orderEntity, totalAmount);
    paymentRepository.save(paymentEntity);

    return savedOrder.toOrder();
  }

  private static void validationOrder(OrderCreationCommand command) {
    if (command.quantityPerProduct().quantity() > 20) {
      throw new BusinessRuleViolation("주문 1회 당 최대 주문 가능 수량인 20개를 초과했습니다.");
    }
  }
}
