package com.quickdeal.purchase.service;

import com.quickdeal.purchase.domain.Order;
import com.quickdeal.purchase.domain.OrderCreationCommand;
import com.quickdeal.purchase.domain.OrderStatusType;
import com.quickdeal.purchase.domain.PaymentStatus;
import com.quickdeal.purchase.domain.PaymentStatusType;
import com.quickdeal.purchase.domain.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PurchaseHandlerService {

  private final PaymentService paymentService;
  private final TicketService ticketService;
  private final OrderService orderService;
  private final InMemoryService inMemoryService;
  private final Logger log;

  public PurchaseHandlerService(PaymentService paymentService, TicketService ticketService,
      OrderService orderService, InMemoryService inMemoryService) {
    this.paymentService = paymentService;
    this.ticketService = ticketService;
    this.orderService = orderService;
    this.inMemoryService = inMemoryService;
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  // :: 주문을 생성하고, 대기열 토큰을 발급하고, 큐잉을 진행함
  // TODO: rdb + kafka 간 트랜잭션 적용
  public Ticket getTicket(OrderCreationCommand command) {
    String userId = command.userId();
    Long productId = command.quantityPerProduct().productId();
    Integer quantity = command.quantityPerProduct().quantity();

    Order order = orderService.saveOrderAndPaymentInitialData(userId, productId, quantity);
    log.debug(
        "[getTicket] finished saveOrderAndPaymentInitialData, userId: {}, orderId : {}, orderStatus : {}",
        userId, order.id(), order.status());
    return ticketService.issueTicket(userId, productId, order.id());
  }

  // :: 결제 진행 후 주문, 결제 상태 업데이트
  // TODO: rdb, 레디스 간 트랜잭션 필요
  public PaymentStatus payment(Long orderId, Long productId, Integer paymentAmount,
      String userId) {
    PaymentStatus status = paymentService.getPaymentStatus(orderId, productId, paymentAmount);
    log.debug("[payment-service] finished payment, status: {}, orderId: {}", status.status(),
        orderId);

    if (status.isPaymentCompleted()) {
      handlePaymentCompleteStatus(orderId, productId, userId);
    } else if (status.isItemSoldOut()) {
      updatePaymentCancellationStatus(orderId, productId, userId);
    } else {
      handlePaymentFailed(orderId, productId, userId);
    }
    return status;
  }

  // TODO: rdb, 레디스 간 트랜잭션 필요
  private void handlePaymentFailed(Long orderId, Long productId, String userId) {
    log.debug("[handlePaymentFailed] payment not completed. userID: {}", userId);
    orderService.updateOrderAndPaymentStatus(orderId, OrderStatusType.ERROR,
        PaymentStatusType.ERROR);
    inMemoryService.removePaymentPageUser(productId, userId);
  }

  // TODO: rdb, 레디스 간 트랜잭션 필요
  private void handlePaymentCompleteStatus(Long orderId, Long productId, String userId) {
    log.debug("[handlePaymentCompleteStatus] payment completed. userID: {}", userId);
    orderService.updateOrderAndPaymentStatus(orderId, OrderStatusType.DONE, PaymentStatusType.DONE);
    inMemoryService.removePaymentPageUser(productId, userId);
  }

  // :: 결제 취소 - 주문 / 결제 정보업데이트
  // TODO: rdb, 레디스 간 트랜잭션 필요
  public void updatePaymentCancellationStatus(Long orderId, Long productId, String userId) {
    log.debug("[handleCancelPayment] cancel. userID: {}", userId);
    orderService.updateOrderAndPaymentStatus(orderId, OrderStatusType.CANCEL,
        PaymentStatusType.CANCEL);
    inMemoryService.removePaymentPageUser(productId,
        userId); //TODO: 컨슘 전(대기 창)에 취소를 하면 해당 로직이 무효가 됨
  }
}
