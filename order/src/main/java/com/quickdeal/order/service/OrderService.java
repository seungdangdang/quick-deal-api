package com.quickdeal.order.service;

import com.quickdeal.common.exception.BusinessRuleViolation;
import com.quickdeal.common.service.ProductService;
import com.quickdeal.order.infrastructure.entity.OrderEntity;
import com.quickdeal.order.infrastructure.entity.OrderProductEntity;
import com.quickdeal.order.infrastructure.entity.PaymentEntity;
import com.quickdeal.order.infrastructure.repository.OrderProductRepository;
import com.quickdeal.order.infrastructure.repository.OrderRepository;
import com.quickdeal.order.infrastructure.repository.PaymentRepository;
import com.quickdeal.order.domain.Order;
import com.quickdeal.order.domain.OrderCreationCommand;
import com.quickdeal.order.domain.QuantityPerProduct;
import java.util.List;
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
    if (command.quantityPerProducts().size() > 20) {
      throw new BusinessRuleViolation("주문 상품이 20개를 초과할 수 없습니다");
    }

    OrderEntity orderEntity = OrderEntity.createOrder(command.userUUID());

    // 주문 저장
    OrderEntity savedOrder = orderRepository.save(orderEntity);

    // 주문-상품 저장
    for (QuantityPerProduct productOne : command.quantityPerProducts()) {
      Long productId = productOne.productId();
      int price = productService.getPriceById(productId);

      OrderProductEntity orderProductEntity = OrderProductEntity.createOrderProduct(savedOrder,
          productId, productOne.quantity(), price);
      orderProductRepository.save(orderProductEntity);
    }

    // 결제 정보 저장
    List<OrderProductEntity> orderProducts = orderProductRepository.findByOrderId(
        savedOrder.getId());

    int totalAmount = orderProducts.stream()
        .mapToInt(op -> op.getPrice() * op.getQuantity())
        .sum();

    PaymentEntity paymentEntity = PaymentEntity.createPayment(orderEntity, totalAmount);
    paymentRepository.save(paymentEntity);

    return savedOrder.toOrder();
  }
}
