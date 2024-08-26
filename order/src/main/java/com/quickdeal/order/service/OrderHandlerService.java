package com.quickdeal.order.service;

import com.quickdeal.order.api.resource.CheckoutStatus;
import com.quickdeal.order.api.resource.CheckoutStatusResult;
import com.quickdeal.order.api.resource.QueueCommand;
import com.quickdeal.order.domain.PaymentCommand;
import com.quickdeal.order.domain.QueueMessage;
import com.quickdeal.order.domain.QueueToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderHandlerService {

  private final QueueService queueService;
  private final TokenService tokenService;
  private final PaymentService paymentService;
  private final MessageQueueProducer messageQueueService;

  public OrderHandlerService(QueueService queueService, TokenService tokenService,
      PaymentService paymentService,
      MessageQueueProducer messageQueueService) {
    this.queueService = queueService;
    this.tokenService = tokenService;
    this.paymentService = paymentService;
    this.messageQueueService = messageQueueService;
  }

  public QueueToken generateQueue(QueueCommand command) {
    // 마지막 요청 대기번호 업데이트 및 얻기
    Long newQueueNumber = queueService.getNewQueueNumber(command.productId());

    // 새로운 대기열 토큰 발급
    QueueToken queueToken = tokenService.generateQueueNumber(command.productId(),
        command.userUUID(), newQueueNumber);

    // queue 메시지 삽입
    QueueMessage queueMessage = new QueueMessage(queueToken.queueNumber(), queueToken.productId(),
        queueToken.userUUID(), queueToken.jwtToken());
    messageQueueService.publishMessage("queue-" + command.productId(), queueMessage);

    return queueToken;
  }

  // todo - rdb, 레디스 간 트랜잭션 필요
  @Transactional
  public CheckoutStatusResult processCheckout(PaymentCommand command) { // TODO: 반환값 타입 수정
    CheckoutStatusResult result = paymentService.checkout(command);

    if (result.status() == CheckoutStatus.DONE_CHECKOUT) {
      paymentService.endedCheckout(command.orderId());
      queueService.decrementPaymentPageUserCount(command.productId());
    } else if (result.status() == CheckoutStatus.ERROR) {
      paymentService.errorCheckout(command.orderId());
      queueService.decrementPaymentPageUserCount(command.productId());
    }

    return result;
  }

  // todo - rdb, 레디스 간 트랜잭션 필요
  @Transactional
  public void cancelCheckout(Long orderId) {
    paymentService.cancelCheckout(orderId);
    queueService.decrementPaymentPageUserCount(orderId);
  }
}
