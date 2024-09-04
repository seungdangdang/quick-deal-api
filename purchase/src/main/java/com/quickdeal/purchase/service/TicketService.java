package com.quickdeal.purchase.service;

import static com.quickdeal.purchase.util.LuaUtil.loadLuaScript;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quickdeal.common.exception.MaxUserLimitExceededException;
import com.quickdeal.common.service.ProductService;
import com.quickdeal.purchase.domain.PageAccessStatuses;
import com.quickdeal.purchase.domain.PaymentPageAccessStatus;
import com.quickdeal.purchase.domain.QueueMessage;
import com.quickdeal.purchase.domain.Ticket;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.exceptions.JedisDataException;

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
  private final Duration renewalThreshold;
  private final Duration extensionDuration;

  public TicketService(@Value("${retry.delay}") long retryDelay,
      @Value("${retry.limit}") long retryLimit,
      @Value("${payment.page.max-users}") int maxPaymentPageUsers,
      @Value("${queue.topic-header}") String topicHeaderStr,
      @Value("${ticket-token.renewal-threshold}") Duration renewalThreshold,
      @Value("${ticket-token.extension-duration}") Duration extensionDuration,
      TokenService tokenService,
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
    this.renewalThreshold = renewalThreshold;
    this.extensionDuration = extensionDuration;
  }

  // 레디스를 통해 티켓 번호 증가하여 get > 토큰 발급 > 카푸카 메시지 발생 > (에러생길시) 레디스의 마지막 티켓 번호 감소
  // :: 대기열 토큰 발급
  // :: 메시지큐 대기 메시지 삽입
  //TODO - 카프카 작업 트랜잭션 필요
  public Ticket issueTicket(String userId, Long productId, Long orderId) {
    Long newTicketNumber = redisService.getNewTicketNumber(productId);
    try {
      Ticket ticket = tokenService.generateTicketNumber(productId, userId, newTicketNumber,
          orderId);
      tokenService.validateTokenAndGetClaims(ticket.ticketToken());

      QueueMessage queueMessage = new QueueMessage(newTicketNumber, productId, userId,
          ticket.ticketToken());

      messageQueueService.publishMessage(topicHeader + productId, queueMessage);

      return ticket;
    } catch (Exception e) {
      log.error("[issueTicket] failed to issueTicket", e);
      redisService.decrementLastTicketNumber(productId);
      log.error("[issueTicket] failed > finished redis rollback");
      throw e;
    }
  }

  // :: 대기열 상태 확인 (캐싱된 재고가 없다면, 재고 없다는 내용 반환 | 재고가 있고 남은 앞 대기자가 없다면 진입 가능 상태 반환 | 재고가 있고 앞 대기자가 있다면 진입 불가능 상태 반환)
  public PaymentPageAccessStatus getPaymentPageAccessStatusByTicket(String ticketToken) {
    Claims claims = tokenService.validateTokenAndGetClaims(ticketToken);

    Long productId = claims.get("product_id", Long.class);
    Long queueNumber = claims.get("queue_number", Long.class);
    log.debug(
        "[getPaymentPageAccessStatusByTicket] start checkQueueStatus, orderId: {}, queueNumber: {}",
        claims.get("order_id"), queueNumber);

    if (!productService.hasCachingStockQuantityById(productId)) {
      log.debug("[getPaymentPageAccessStatusByTicket] caching stock quantity");
      return new PaymentPageAccessStatus(PageAccessStatuses.ITEM_SOLD_OUT, null, null);
    }

    Long lastExitedQueueNumber = redisService.getLastExitedQueueNumber(productId);
    long remainingInQueue = queueNumber - lastExitedQueueNumber;
    log.debug(
        "[getPaymentPageAccessStatusByTicket] this queueNumber: {}, lastExitedQueueNumber: {}, remainingInQueue: {}",
        queueNumber, lastExitedQueueNumber, remainingInQueue);

    if (remainingInQueue <= 0) {
      log.debug("[getPaymentPageAccessStatusByTicket] ACCESS_GRANTED, orderId: {}",
          claims.get("order_id"));
      return new PaymentPageAccessStatus(PageAccessStatuses.ACCESS_GRANTED, 0L, null);
    } else {
      log.debug("[getPaymentPageAccessStatusByTicket] ACCESS_DENIED, orderId: {}",
          claims.get("order_id"));
      String renewToken = renewTokenIfExpiringSoon(claims, ticketToken);
      log.debug("[getPaymentPageAccessStatusByTicket] ACCESS_DENIED, renewToken: {}",
          renewToken);
      return new PaymentPageAccessStatus(PageAccessStatuses.ACCESS_DENIED, remainingInQueue,
          renewToken);
    }
  }

  // :: 토큰 만료시간이 30분 이하면, 1시간 연장 토큰 발행 / 아니라면 기존 토큰 재사용
  private String renewTokenIfExpiringSoon(Claims claims, String jwtToken) {
    Date expiration = claims.getExpiration();
    long now = System.currentTimeMillis();
    long timeUntilExpiration = expiration.getTime() - now;

    // 갱신 기준 시간을 설정 값으로 가져옴
    if (timeUntilExpiration <= renewalThreshold.toMillis()) {
      // 연장 시간을 설정 값으로 가져옴
      return tokenService.extendTicketJwtExpiration(jwtToken, extensionDuration.toMillis());
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
    validateAccessWithRetryLimit(message.userUUID(), message.productId(), message.ticketNumber());
  }

  // :: 페이지 액세스 확인 with 재실행
  public void validateAccessWithRetryLimit(String userId, Long productId, Long ticketNumber)
      throws InterruptedException {
    for (int i = 0; i < retryLimit; i++) {
      try {
        String luaScript = loadLuaScript(
            "purchase/src/main/java/com/quickdeal/purchase/service/script.lua");
        List<String> keys = Collections.singletonList(productId.toString());
        List<String> args = Arrays.asList(ticketNumber.toString(), userId,
            String.valueOf(maxPaymentPageUsers));

        Object result = redisService.executeLuaScript(luaScript, keys, args);
        int success = Integer.parseInt(result.toString());

        if (success == 1) {
          productService.decreaseStockQuantityById(productId);
          return;
        }
      } catch (IOException e) {
        log.error("Lua 스크립트 실행 중 네트워크 오류 발생");
      } catch (JedisDataException e) {
        log.error("Lua 스크립트 실행 중 redis.call 오류 발생");
      }
      Thread.sleep(retryDelay);
    }
    throw new MaxUserLimitExceededException("결제페이지의 최대 사용자 용량에 도달했습니다.");
  }
}
