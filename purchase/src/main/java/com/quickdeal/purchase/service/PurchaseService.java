package com.quickdeal.purchase.service;

import com.quickdeal.common.exception.OrderTimeExpiredException;
import com.quickdeal.purchase.domain.Order;
import com.quickdeal.purchase.domain.OrderCreationCommand;
import com.quickdeal.purchase.domain.OrderStatusType;
import com.quickdeal.purchase.domain.PaymentStatus;
import com.quickdeal.purchase.domain.PaymentStatusType;
import com.quickdeal.purchase.outbound.redis.repository.OrderPaymentSlotRedisRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {

  private final PaymentService paymentService;
  private final OrderService orderService;
  private final OrderPaymentSlotRedisRepository orderPaymentSlotRedisRepository;
  private final Logger log;

  public PurchaseService(PaymentService paymentService,
      OrderService orderService, OrderPaymentSlotRedisRepository orderPaymentSlotRedisRepository) {
    this.paymentService = paymentService;
    this.orderService = orderService;
    this.orderPaymentSlotRedisRepository = orderPaymentSlotRedisRepository;
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  public Order saveOrder(OrderCreationCommand command) {
    // 주문 만료 시간이 지났는지 검증
    if (isOrderExpired(command)) {
      throw new OrderTimeExpiredException("주문 가능 시간이 만료되었습니다.");
    }
    return orderService.saveOrderAndPaymentInitialData(command);
  }

  @Transactional
  // :: 결제 진행 후 주문, 결제 상태 업데이트
  public PaymentStatus paymentAndGetPaymentStatus(Long orderId, Long productId,
      Integer paymentAmount,
      String userId) {
    PaymentStatus status = paymentService.getPaymentStatus(orderId, productId, paymentAmount);
    log.debug("[payment-service] finished payment, status: {}, orderId: {}", status.status(),
        orderId);

    if (status.isPaymentCompleted()) {
      saveDone(orderId, productId, userId);
    } else if (status.isItemSoldOut()) {
      saveCancel(orderId, productId, userId);
    } else {
      saveError(orderId, productId, userId);
    }
    return status;
  }

  private boolean isOrderExpired(OrderCreationCommand command) {
    Long productId = command.quantityPerProduct().productId();
    String userId = command.userId();
    Long expirationTime = orderPaymentSlotRedisRepository.getPaymentPageUserSortedSetScore(
        productId,
        userId
    ).orElseThrow(() -> {
      log.error(
          "[getUserExpirationTime] redis sorted set score is null."
          + ", valueForSearch(userId): {}, productId: {}",
          userId,
          productId
      );

      return new IllegalStateException(
          "[getUserExpirationTime] redis sorted set score is null. key: "
          + ", valueForSearch(userId): " + userId +
          ", productId: " + productId
      );
    });
    long currentTimeInSeconds = Instant.now().getEpochSecond();

    boolean isOrderExpired = expirationTime != null && expirationTime < currentTimeInSeconds;
    log.debug(
        "[isOrderExpired] userExpirationTime fetched. userExpirationTime: {}, command: {}, isOrderExpired: {}",
        currentTimeInSeconds, command, isOrderExpired);
    return isOrderExpired;
  }

  // :: 결제 실패 - 주문 / 결제 정보 업데이트
  private void saveError(Long orderId, Long productId, String userId) {
    log.debug("[handlePaymentFailed] payment not completed. userID: {}", userId);
    orderService.updateOrderAndPaymentStatus(orderId, OrderStatusType.ERROR,
        PaymentStatusType.ERROR);
    orderPaymentSlotRedisRepository.removePaymentPageUser(productId, userId);
  }

  // :: 결제 완료 - 주문 / 결제 정보 업데이트
  private void saveDone(Long orderId, Long productId, String userId) {
    log.debug("[handlePaymentCompleteStatus] payment completed. userID: {}", userId);
    orderService.updateOrderAndPaymentStatus(orderId, OrderStatusType.DONE, PaymentStatusType.DONE);
    orderPaymentSlotRedisRepository.removePaymentPageUser(productId, userId);
  }

  // :: 결제 취소 - 주문 / 결제 정보 업데이트
  public void saveCancel(Long orderId, Long productId, String userId) {
    log.debug("[handleCancelPayment] cancel. userID: {}", userId);
    orderService.updateOrderAndPaymentStatus(orderId, OrderStatusType.CANCEL,
        PaymentStatusType.CANCEL);
    orderPaymentSlotRedisRepository.removePaymentPageUser(productId, userId);
  }
}
