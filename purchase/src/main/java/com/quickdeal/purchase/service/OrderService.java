package com.quickdeal.purchase.service;

import com.quickdeal.common.exception.OrderStatusInvalidException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

  private final Logger log;
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
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  // :: 주문과 결제 초기 데이터 저장
  @Transactional
  public Order saveOrderAndPaymentInitialData(String userUUID, Long productId, Integer quantity) {
    int price = productService.getPriceById(productId);
    int totalAmount = price * quantity;

    OrderEntity orderEntity = OrderEntity.createOrder(userUUID);

    OrderProductEntity orderProductEntity = OrderProductEntity.createOrderProduct(orderEntity,
        productId, quantity, price);

    PaymentEntity paymentEntity = PaymentEntity.createPayment(orderEntity, totalAmount);

    OrderEntity savedOrder = orderRepository.save(orderEntity);

    orderProductRepository.save(orderProductEntity);

//    paymentService.createPayment(paymentEntity);
    PaymentEntity savedPaymentEntity = paymentService.createPayment(paymentEntity);
    log.debug("[saveOrderAndPaymentInitialData] creteOrderInitial: {}, createPaymentInitial: {}",
        orderEntity, savedPaymentEntity.toString());

    return savedOrder.toOrder();
  }

  @Transactional
  public OrderInfo getOrderInfo(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new EntityNotFoundException("해당 주문을 찾을 수 없습니다: " + orderId)).toOrder();

    OrderProduct orderProduct = orderProductRepository.findByOrderId(orderId).toOrderProduct();

    Product product = productService.getProduct(orderProduct.productId());

    OrderProductInfo orderProductInfo = new OrderProductInfo(orderProduct.id(), product.name(),
        orderProduct.quantity(), orderProduct.price());

    Integer amount = orderProductInfo.price() * orderProductInfo.quantity();

    return new OrderInfo(order.id(), orderProductInfo, amount);
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

  @Transactional
  public void validateAvailableOrder(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new EntityNotFoundException("해당 주문을 찾을 수 없습니다: " + orderId)).toOrder();
    if (order.status() != OrderStatusType.PROCESSING) {
      throw new OrderStatusInvalidException("유효하지 않은 주문입니다.");
    }
  }
}
