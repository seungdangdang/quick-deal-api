package com.quickdeal.purchase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quickdeal.common.exception.MaxUserLimitExceededException;
import com.quickdeal.common.service.ProductService;
import com.quickdeal.purchase.domain.OrderCreationCommand;
import com.quickdeal.purchase.domain.PageAccessStatusType;
import com.quickdeal.purchase.domain.PaymentPageAccessStatus;
import com.quickdeal.purchase.domain.QueueMessage;
import com.quickdeal.purchase.domain.QueuePollingCommand;
import com.quickdeal.purchase.domain.Ticket;
import io.jsonwebtoken.Claims;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

  public TicketService(@Value("${retry.delay}") long retryDelay,
      @Value("${retry.limit}") long retryLimit,
      @Value("${payment.max-users}") int maxPaymentPageUsers,
      @Value("${queue.topic-header}") String topicHeaderStr, TokenService tokenService,
      ProductService productService, MessageQueueProducer messageQueueService,
      RedisService redisService) {
    this.retryDelay = retryDelay;
    this.retryLimit = retryLimit;
    this.maxPaymentPageUsers = maxPaymentPageUsers;
    this.topicHeader = topicHeaderStr;
    this.tokenService = tokenService;
    this.productService = productService;
    this.messageQueueService = messageQueueService;
    this.redisService = redisService;
  }

  // :: 대기열 토큰 발급
  // :: 메시지큐 대기 메시지 삽입
  public Ticket issueTicket(OrderCreationCommand command) {
    long productId = command.quantityPerProduct().productId();
    Long newTicketNumber = redisService.getNewTicketNumber(productId);
    Ticket ticket = tokenService.generateTicketNumber(productId, command.userUUID(),
        newTicketNumber);

    QueueMessage queueMessage = new QueueMessage(ticket.ticketNumber(), ticket.productId(),
        ticket.userUUID(), ticket.jwtToken());
    messageQueueService.publishMessage(topicHeader + productId, queueMessage);

    return ticket;
  }

  // :: 대기열 상태 확인 (캐싱된 재고가 없다면, 재고 없다는 내용 반환 | 재고가 있고 남은 앞 대기자가 없다면 진입 가능 상태 반환 | 재고가 있고 앞 대기자가 있다면 진입 불가능 상태 반환)
  public PaymentPageAccessStatus getPaymentPageAccessStatusByTicket(QueuePollingCommand command) {
    if (!productService.hasCachingStockQuantityById(command.productId())) {
      return new PaymentPageAccessStatus(PageAccessStatusType.ITEM_SOLD_OUT, null, null);
    }

    Long lastExitedQueueNumber = redisService.getLastExitedQueueNumber(command.productId());
    Long requestQueueNumber = command.ticketNumber();
    long remainingInQueue = requestQueueNumber - lastExitedQueueNumber;

    Claims claims = tokenService.validateTokenAndGetClaims(command.jwtToken());

    if (remainingInQueue <= 0) {
      return new PaymentPageAccessStatus(PageAccessStatusType.ACCESS_GRANTED, 0L, null);
    } else {
      String renewToken = renewTokenIfExpiringSoon(claims, command.jwtToken());
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

  // :: 페이지 접근 가능 여부 확인 t/f
  public void validateTicketAndPaymentPageAccessible(QueueMessage message)
      throws JsonProcessingException, InterruptedException {
    // 상품 아이디 검증
    productService.getProduct(message.productId());
    // 유효 토큰 검증
    tokenService.validateTokenAndGetClaims(message.ticketToken());
    // 페이지 접속자 확인
    validateAccessWithRetryLimit(message.productId(), message.ticketNumber());
  }

  // :: 페이지 액세스 확인 with 재실행
  private void validateAccessWithRetryLimit(Long productId, Long ticketNumber)
      throws InterruptedException {

    for (int i = 0; i < retryLimit; i++) {
      Integer accessorCount = redisService.getCurrentPaymentPageUserCount(productId);
      // 사용자 수가 최대값보다 적은지 확인
      if (accessorCount < maxPaymentPageUsers) {
        // 레디스에서 접속현황 업데이트히여 세마포어 확보
        redisService.incrementPaymentPageUserCount(productId);
        // 레디스에서 마지막으로 대기열을 나간 사용자 번호 업데이트
        redisService.updateLastExitedTicketNumber(productId, ticketNumber);
        // 해당 상품의 재고 1개 미리 확보
        productService.decreaseStockQuantityById(productId);
        return;
      }
      Thread.sleep(retryDelay);
    }
    throw new MaxUserLimitExceededException("결제페이지의 최대 사용자 용량에 도달했습니다.");

  }
}
