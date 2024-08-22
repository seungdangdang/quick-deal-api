package com.quickdeal.order.service;

import com.quickdeal.order.api.resource.QueueCommand;
import com.quickdeal.order.domain.QueueMessage;
import com.quickdeal.order.domain.QueueToken;
import org.springframework.stereotype.Service;

@Service
public class OrderHandlerService {

  private final QueueService queueService;
  private final TokenService tokenService;
  private final MessageQueueService messageQueueService;

  public OrderHandlerService(QueueService queueService, TokenService tokenService,
      MessageQueueService messageQueueService) {
    this.queueService = queueService;
    this.tokenService = tokenService;
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
        queueToken.userUUID());
    messageQueueService.publishMessage("queue", queueMessage);

    return queueToken;
  }
}
