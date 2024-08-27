package com.quickdeal.purchase.service;

import com.quickdeal.common.service.ProductService;
import com.quickdeal.common.service.domain.Product;
import com.quickdeal.purchase.domain.Order;
import com.quickdeal.purchase.domain.OrderInfo;
import com.quickdeal.purchase.domain.OrderProduct;
import com.quickdeal.purchase.domain.OrderProductInfo;
import com.quickdeal.purchase.domain.OrderStatusType;
import com.quickdeal.purchase.domain.PaymentStatusType;
import com.quickdeal.purchase.infrastructure.entity.OrderEntity;
import com.quickdeal.purchase.infrastructure.entity.OrderProductEntity;
import com.quickdeal.purchase.infrastructure.entity.PaymentEntity;
import com.quickdeal.purchase.infrastructure.repository.OrderProductRepository;
import com.quickdeal.purchase.infrastructure.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductService productService;
  private final PaymentService paymentService;
  private final OrderProductRepository orderProductRepository;

  public OrderService(OrderRepository orderRepository, ProductService productService,
      PaymentService paymentService, OrderProductRepository orderProductRepository) {
    this.orderRepository = orderRepository;
    this.productService = productService;
    this.paymentService = paymentService;
    this.orderProductRepository = orderProductRepository;
  }

  // :: 주문과 결제 초기 데이터 저장
  @Transactional
  public void saveOrderAndPaymentInitialData(String userUUID, Long productId, Integer quantity) {
    OrderEntity orderEntity = OrderEntity.createOrder(userUUID);

    // 주문 저장
    OrderEntity savedOrder = orderRepository.save(orderEntity);

    // 주문-상품 저장
    int price = productService.getPriceById(productId);
    OrderProductEntity orderProductEntity = OrderProductEntity.createOrderProduct(savedOrder,
        productId, quantity, price);
    orderProductRepository.save(orderProductEntity);

    // 결제 정보 저장
    OrderProductEntity orderProducts = orderProductRepository.findByOrderId(savedOrder.getId());
    int totalAmount = orderProducts.getPrice() * orderProducts.getQuantity();
    PaymentEntity paymentEntity = PaymentEntity.createPayment(orderEntity, totalAmount);
    paymentService.createPayment(paymentEntity);
  }

  @Transactional
  public OrderInfo getOrderInfo(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new EntityNotFoundException("해당 주문을 찾을 수 없습니다: " + orderId))
        .toOrder();

    OrderProduct orderProduct = orderProductRepository.findByOrderId(orderId).toOrderProduct();

    Product product = productService.getProduct(orderProduct.id());

    OrderProductInfo orderPRoductInfo = new OrderProductInfo(
        orderProduct.id(),
        product.name(),
        orderProduct.quantity(),
        orderProduct.price()
    );

    return new OrderInfo(order.id(), orderPRoductInfo);
  }

  @Transactional
  public void updateOrderAndPaymentStatus(Long orderId, OrderStatusType orderStatus,
      PaymentStatusType paymentStatus) {
    // 주문 상태 업데이트
    orderRepository.updateOrderStatus(orderId, orderStatus);

    // 결제 상태 업데이트
    if (orderStatus == OrderStatusType.DONE) {
      paymentService.updatePaymentStatus(orderId, Instant.now(), paymentStatus);
    } else {
      paymentService.updatePaymentStatus(orderId, null, paymentStatus);
    }

    // 주문 취소 또는 오류 시 재고 증가
    if (orderStatus == OrderStatusType.CANCEL || orderStatus == OrderStatusType.ERROR) {
      productService.increaseStockQuantityById(orderId);
    }
  }
}
