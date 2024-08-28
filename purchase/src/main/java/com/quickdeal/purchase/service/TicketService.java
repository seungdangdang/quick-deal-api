package com.quickdeal.purchase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quickdeal.common.exception.MaxUserLimitExceededException;
import com.quickdeal.common.service.ProductService;
import com.quickdeal.purchase.domain.PageAccessStatusType;
import com.quickdeal.purchase.domain.PaymentPageAccessStatus;
import com.quickdeal.purchase.domain.QueueMessage;
import com.quickdeal.purchase.domain.Ticket;
import io.jsonwebtoken.Claims;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

  private final TokenService tokenService;
  private final long retryDelay;
  private final long retryLimit;
  private final int maxPaymentPageUsers;
  private final String topicHeader;
  private final ProductService productService;
  private final MessageQueueProducer messageQueueService;
  private final RedisService redisService;
  private final OrderService orderService;
  private final Logger log;

  public TicketService(@Value("${retry.delay}") long retryDelay,
      @Value("${retry.limit}") long retryLimit,
      @Value("${payment.max-users}") int maxPaymentPageUsers,
      @Value("${queue.topic-header}") String topicHeaderStr, TokenService tokenService,
      ProductService productService, MessageQueueProducer messageQueueService,
      RedisService redisService, OrderService orderService) {
    this.retryDelay = retryDelay;
    this.retryLimit = retryLimit;
    this.maxPaymentPageUsers = maxPaymentPageUsers;
    this.topicHeader = topicHeaderStr;
    this.tokenService = tokenService;
    this.productService = productService;
    this.messageQueueService = messageQueueService;
    this.redisService = redisService;
    this.orderService = orderService;
    this.log = LoggerFactory.getLogger(this.getClass());
  }


  // 레디스를 통해 티켓 번호 증가하여 get > 토큰 발급 > 카푸카 메시지 발생 > (에러생길시) 레디스의 마지막 티켓 번호 감소
  // :: 대기열 토큰 발급
  // :: 메시지큐 대기 메시지 삽입
  //TODO - 카프카 작업 트랜잭션 필요
  public Ticket issueTicket(String userUUID, Long productId, Long orderId) {
    Long newTicketNumber = redisService.getNewTicketNumber(productId);
    try {
      Ticket ticket = tokenService.generateTicketNumber(productId, userUUID, newTicketNumber,
          orderId);

      QueueMessage queueMessage = new QueueMessage(newTicketNumber, productId, userUUID,
          ticket.jwtToken());

      messageQueueService.publishMessage(topicHeader + productId, queueMessage);

      return ticket;
    } catch (Exception e) {
      redisService.decrementLastTicketNumber(productId);

      throw e;
    }
  }

  // :: 대기열 상태 확인 (캐싱된 재고가 없다면, 재고 없다는 내용 반환 | 재고가 있고 남은 앞 대기자가 없다면 진입 가능 상태 반환 | 재고가 있고 앞 대기자가 있다면 진입 불가능 상태 반환)
  public PaymentPageAccessStatus getPaymentPageAccessStatusByTicket(String ticketToken) {
    Claims claims = tokenService.validateTokenAndGetClaims(ticketToken);

    Long productId = claims.get("product_id", Long.class);
    Long queueNumber = claims.get("queue_number", Long.class);

    if (!productService.hasCachingStockQuantityById(productId)) {
      return new PaymentPageAccessStatus(PageAccessStatusType.ITEM_SOLD_OUT, null, null);
    }

    Long lastExitedQueueNumber = redisService.getLastExitedQueueNumber(productId);
    long remainingInQueue = queueNumber - lastExitedQueueNumber;

    if (remainingInQueue <= 0) {
      return new PaymentPageAccessStatus(PageAccessStatusType.ACCESS_GRANTED, 0L, null);
    } else {
      String renewToken = renewTokenIfExpiringSoon(claims, ticketToken);
      return new PaymentPageAccessStatus(PageAccessStatusType.ACCESS_DENIED, remainingInQueue,
          renewToken);
    }
  }

  // :: 토큰 만료시간이 30분 이하면, 1시간 연장 토큰 발행 / 아니라면 기존 토큰 재사용
  private String renewTokenIfExpiringSoon(Claims claims, String jwtToken) {
    Date expiration = claims.getExpiration();
    long now = System.currentTimeMillis();
    long timeUntilExpiration = expiration.getTime() - now;

    if (timeUntilExpiration <= 1000L * 60L * 30L) {
      return tokenService.extendTicketJwtExpiration(jwtToken, 1000L * 60L * 60L);
    } else {
      return jwtToken;
    }
  }

  @Transactional
  // :: 페이지 접근 가능 여부 확인
  public void validateTicketAndPaymentPageAccessible(QueueMessage message)
      throws JsonProcessingException, InterruptedException {
    // 유효 토큰 검증
    Claims claims = tokenService.validateTokenAndGetClaims(message.ticketToken());

    // 진행중인 주문인지 검증
    Long orderId = claims.get("order_id", Long.class);
    orderService.validateAvailableOrder(orderId);

    // 페이지 접속자 확인
    validateAccessWithRetryLimit(message.productId(), message.ticketNumber());
  }

  // TODO: 레디스 트랜잭션 고민
  // :: 페이지 액세스 확인 with 재실행 - redis / rdb
  public void validateAccessWithRetryLimit(Long productId, Long ticketNumber)
      throws InterruptedException {

    for (int i = 0; i < retryLimit; i++) {
      Integer accessorCount = redisService.getCurrentPaymentPageUserCount(productId);
      if (accessorCount < maxPaymentPageUsers) {
        boolean incrementedPageUser = false;
        boolean updatedLastExitedTicketNumber = false;
        Long currentLastExitedQueueNumber = redisService.getLastExitedQueueNumber(productId);

        try {
          // 레디스에서 접속현황 업데이트히여 세마포어 확보
          redisService.incrementPaymentPageUserCount(productId);
          incrementedPageUser = true;

          // 레디스에서 마지막으로 대기열을 나간 사용자 번호 업데이트
          redisService.updateLastExitedTicketNumber(productId, ticketNumber);
          updatedLastExitedTicketNumber = true;

          // 해당 상품의 재고 1개 미리 확보
          productService.decreaseStockQuantityById(productId);

          return;
        } catch (Exception e) {
          try {
            if (incrementedPageUser) {
              redisService.decrementPaymentPageUserCount(productId);
            }
          } catch (Exception e1) {
            log.error("보상 트랜잭션 실패(paymentPageUserCount 복구 실패)");
          }
          try {
            if (updatedLastExitedTicketNumber) {
              redisService.updateLastExitedTicketNumber(productId, currentLastExitedQueueNumber);
            }
          } catch (Exception e2) {
            log.error("보상 트랜잭션 실패(updatedLastExitedTicketNumber 복구 실패)");
          }
          throw e; //TODO: 적절한 예외 던지기
        }
      }
      Thread.sleep(retryDelay);
    }
    throw new MaxUserLimitExceededException("결제페이지의 최대 사용자 용량에 도달했습니다.");
  }
}
