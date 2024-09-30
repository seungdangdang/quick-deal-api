package com.quickdeal.purchase.service;

import com.quickdeal.common.exception.MaxUserLimitExceededException;
import com.quickdeal.common.service.ProductService;
import com.quickdeal.purchase.config.OrderCreationProperties;
import com.quickdeal.purchase.domain.OrderTicket;
import com.quickdeal.purchase.domain.PageAccessStatuses;
import com.quickdeal.purchase.domain.PaymentPageAccessStatus;
import com.quickdeal.purchase.domain.QueueMessage;
import com.quickdeal.purchase.outbound.redis.repository.OrderPaymentSlotRedisRepository;
import com.quickdeal.purchase.outbound.redis.repository.OrderTicketNumberRedisRepository;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableConfigurationProperties({OrderCreationProperties.class})
public class OrderTicketService {

  private final Logger log;
  private final long retryDelay;
  private final long retryLimit;
  private final int maxPaymentPageUsers;
  private final OrderTicketTokenService orderTicketTokenService;
  private final ProductService productService;
  private final MessageQueueProducer messageQueueService;
  private final OrderTicketNumberRedisRepository orderTicketNumberRedisRepository;
  private final OrderPaymentSlotRedisRepository orderPaymentSlotRedisRepository;
  private final ProductTopicMappingService productTopicMappingService;

  public OrderTicketService(
      OrderCreationProperties orderCreationProperties,
      OrderTicketTokenService orderTicketTokenService,
      ProductService productService,
      MessageQueueProducer messageQueueService,
      OrderTicketNumberRedisRepository orderTicketNumberRedisRepository,
      OrderPaymentSlotRedisRepository orderPaymentSlotRedisRepository,
      ProductTopicMappingService productTopicMappingService) {
    this.orderTicketNumberRedisRepository = orderTicketNumberRedisRepository;
    this.orderPaymentSlotRedisRepository = orderPaymentSlotRedisRepository;
    this.productTopicMappingService = productTopicMappingService;
    this.log = LoggerFactory.getLogger(this.getClass());
    this.retryDelay = orderCreationProperties.getRetryDelay();
    this.retryLimit = orderCreationProperties.getRetryLimit();
    this.maxPaymentPageUsers = orderCreationProperties.getMaxConcurrentUsers();
    this.productService = productService;
    this.orderTicketTokenService = orderTicketTokenService;
    this.messageQueueService = messageQueueService;
  }

  // 레디스를 통해 티켓 번호 증가하여 get > 토큰 발급 > 카푸카 메시지 발생 > (에러생길시) 레디스의 마지막 티켓 번호 감소
  // :: 대기열 토큰 발급
  // :: 메시지큐 대기 메시지 삽입
  public OrderTicket issueOrderTicket(String userId, Long productId) {
    Long issuedTicketNumber = orderTicketNumberRedisRepository.increaseLastTicketNumber(productId);
    try {
      OrderTicket orderTicket = orderTicketTokenService.generateTicketNumber(productId, userId,
          issuedTicketNumber);

      QueueMessage queueMessage = new QueueMessage(issuedTicketNumber, productId, userId,
          orderTicket.stringToken());
      messageQueueService.publishMessage(
          productTopicMappingService.getMappedTopicByProductId(productId), queueMessage);
      log.debug("[issueOrderTicket][{}] ticket issued. issuedTicket: {}",
          userId,
          issuedTicketNumber);

      return orderTicket;
    } catch (Exception e) {
      orderTicketNumberRedisRepository.decreaseTicketNumber(productId);
      log.error(
          "[issueTicket] failed to issueTicket > finished redis rollback, userId: {}, productId:{}",
          userId, productId);
      throw e;
    }
  }

  // :: 대기열 상태 확인 (캐싱된 재고가 없다면, 재고 없다는 내용 반환 | 재고가 있고 남은 앞 대기자가 없다면 진입 가능 상태 반환 | 재고가 있고 앞 대기자가 있다면 진입 불가능 상태 반환)
  public PaymentPageAccessStatus getPaymentPageAccessStatus(String queueTicketToken) {
    Claims claims = orderTicketTokenService.validateTokenAndGetClaims(queueTicketToken);

    Long productId = claims.get("product_id", Long.class);
    Long ticketNumber = claims.get("ticket_number", Long.class);
    String userId = claims.get("user_id", String.class);
    log.debug(
        "[getPaymentPageAccessStatusByTicket] start checkQueueStatus, ticketNumber: {}, userId: {}",
        ticketNumber, userId);

    if (!productService.hasCachingStockQuantityById(productId)) {
      log.debug(
          "[getPaymentPageAccessStatusByTicket] caching stock quantity ticketNumber: {}, userId: {}",
          ticketNumber, userId);
      return new PaymentPageAccessStatus(PageAccessStatuses.ITEM_SOLD_OUT, null, null);
    }

    Long lastExitedTicketNumber = orderPaymentSlotRedisRepository.getLastExitedTicketNumber(
        productId);

    // kafka 파티셔닝으로 인해 sortedSet에 [2, 3, 4]가 저장돼 있을 때 1유저가 조회하려고 하면 음수가 나올 수 있음
    // 완벽한 순서 보장은 안되기 때문에 remainingInQueue로 분기를 하는 비즈니스 로직은 금지
    // [2, 3, 4].exists(1) 방식으로 접근해야 함
    long usersAheadNumberOfCurrentUser = ticketNumber - lastExitedTicketNumber;

    var isPayableUser = orderPaymentSlotRedisRepository.existsInPaymentPageUserSortedSet(
        productId,
        userId
    );

    log.debug(
        "[getPaymentPageAccessStatusByTicket] fetched successfully lastExitedTicketNumber. "
        + "lastExitedTicketNumber: {}, usersAheadNumberOfCurrentUser: {}, ticketNumber: {}, userId: {}",
        lastExitedTicketNumber, usersAheadNumberOfCurrentUser, ticketNumber, userId);

    // remainingInQueue가 음수인 경우는 topic 파티셔닝으로 인해 한 두명의 차이로 순서가 뒤바뀔 수 있음
    if (isPayableUser) {
      log.debug("[getPaymentPageAccessStatusByTicket][{}] ticketNumber: {}, userId: {}",
          PageAccessStatuses.ACCESS_GRANTED.name(), ticketNumber, userId);
      return new PaymentPageAccessStatus(PageAccessStatuses.ACCESS_GRANTED, 0L, null);
    } else {
      String renewToken = orderTicketTokenService.refreshTokenIfExpired(queueTicketToken);
      log.debug(
          "[getPaymentPageAccessStatusByTicket][{}] ticket renewed. ticketNumber: {}, userId: {}",
          PageAccessStatuses.ACCESS_DENIED.name(), ticketNumber, userId);
      return new PaymentPageAccessStatus(PageAccessStatuses.ACCESS_DENIED,
          usersAheadNumberOfCurrentUser,
          renewToken);
    }
  }

  @Transactional
  // :: 페이지 접근 가능 여부 확인
  public void validateTicketAndPaymentPageAccessible(QueueMessage message)
      throws InterruptedException {
    // 유효 토큰 검증
    orderTicketTokenService.validateTokenAndGetClaims(message.ticketToken());
    // 페이지 접속자 확인
    validatePaymentPageAccessWithRetryLimit(message.userId(), message.productId(),
        message.ticketNumber());
  }

  // :: 페이지 액세스 확인 with 재실행
  public void validatePaymentPageAccessWithRetryLimit(
      String userId,
      Long productId,
      Long ticketNumber
  ) throws InterruptedException {
    for (int i = 0; i < retryLimit; i++) {
      boolean existsPaymentSlot = orderPaymentSlotRedisRepository.existsPaymentSlot(
          productId,
          ticketNumber,
          userId,
          maxPaymentPageUsers
      );
      if (existsPaymentSlot) {
        productService.decreaseStockQuantityById(productId);
        return;
      }
      Thread.sleep(retryDelay);
    }
    throw new MaxUserLimitExceededException("결제페이지의 최대 사용자 용량에 도달했습니다.");
  }
}
