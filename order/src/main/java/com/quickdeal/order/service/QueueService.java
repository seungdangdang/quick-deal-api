package com.quickdeal.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quickdeal.common.exception.MaxUserLimitExceededException;
import com.quickdeal.common.service.ProductService;
import com.quickdeal.order.api.resource.QueueEntryRequestParams;
import com.quickdeal.order.api.resource.QueuePollingCommand;
import com.quickdeal.order.config.RedisConfig;
import com.quickdeal.order.domain.QueueMessage;
import com.quickdeal.order.domain.QueueStatus;
import io.jsonwebtoken.Claims;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QueueService {

  private final RedisTemplate<String, Long> redisTemplate;
  private final TokenService tokenService;
  private final long retryDelay;
  private final long retryLimit;
  private final int maxPaymentPageUsers;
  private final ProductService productService;

  public QueueService(@Value("${retry.delay}") long retryDelay,
      @Value("${retry.limit}") long retryLimit,
      @Value("${payment.max-users}") int maxPaymentPageUsers,
      RedisTemplate<String, Long> redisTemplate, TokenService tokenService,
      ProductService productService) {
    this.retryDelay = retryDelay;
    this.retryLimit = retryLimit;
    this.maxPaymentPageUsers = maxPaymentPageUsers;
    this.redisTemplate = redisTemplate;
    this.tokenService = tokenService;
    this.productService = productService;
  }

  // 레디스에서 마지막으로 요청 들어온 대기번호 업데이트하는 코드
  public Long getNewQueueNumber(Long productId) {
    String key = RedisConfig.getLastQueueNumberKey(productId);
    Object newQueueNumber = redisTemplate.opsForValue().increment(key, 1);
    return newQueueNumber != null ? Long.parseLong(String.valueOf(newQueueNumber)) : 1L;
  }

  // 레디스에서 마지막으로 대기열에서 빠져나간 대기번호 가져오는 코드
  private Long getLastExitedQueueNumber(Long productId) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    Object value = redisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(String.valueOf(value)) : 0L;
  }

  private void updateLastExitedQueueNumber(Long productId, Long lastExitedQueueNumber) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    redisTemplate.opsForValue().set(key, lastExitedQueueNumber);
  }

  private Integer getCurrentPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    Object value = redisTemplate.opsForValue().get(key);
    return value != null ? Integer.parseInt(String.valueOf(value)) : 0;
  }

  private void incrementPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    redisTemplate.opsForValue().increment(key, 1);
  }

  public void decrementPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    redisTemplate.opsForValue().increment(key, -1);
  }

  public QueueStatus checkQueueStatus(QueuePollingCommand command) {
    if (!productService.hasCachingStockQuantityById(command.productId())) {
      return new QueueStatus(true, false, 0L, null);
    }

    Long lastExitedQueueNumber = getLastExitedQueueNumber(command.productId());
    Long requestQueueNumber = command.queueNumber();
    long remainingInQueue = requestQueueNumber - lastExitedQueueNumber;

    Claims claims = tokenService.validateTokenAndGetClaims(command.jwtToken());

    if (remainingInQueue <= 0) {
      return new QueueStatus(false, true, 0L, null);
    } else {
      String renewToken = renewTokenIfExpiringSoon(claims, command.jwtToken());
      return new QueueStatus(false, false, remainingInQueue, renewToken);
    }
  }

  private String renewTokenIfExpiringSoon(Claims claims, String jwtToken) {
    Date expiration = claims.getExpiration();
    long now = System.currentTimeMillis();
    long timeUntilExpiration = expiration.getTime() - now;

    if (timeUntilExpiration <= 1000L * 60L * 30L) {
      return tokenService.extendQueueJwtExpiration(jwtToken, 1000L * 60L * 60L);
    } else {
      return jwtToken;
    }
  }

  @Transactional
  public void processQueueMessageForPageAccess(QueueMessage message)
      throws JsonProcessingException, InterruptedException {
    // 상품 아이디 검증
    productService.getProduct(message.productId());
    // 유효 토큰 검증
    tokenService.validateTokenAndGetClaims(message.queueToken());
    // 페이지 접속자 확인
    checkAccessWithRetryLimit(
        new QueueEntryRequestParams(message.productId(), message.queueNumber()));
  }

  @Transactional
  public void checkAccessWithRetryLimit(QueueEntryRequestParams request)
      throws InterruptedException {
    for (int i = 0; i < retryLimit; i++) {
      Integer accessorCount = getCurrentPaymentPageUserCount(request.productId());
      // 사용자 수가 최대값보다 적은지 확인
      if (accessorCount < maxPaymentPageUsers) {
        // 레디스에서 최신대기번호 업데이트
        incrementPaymentPageUserCount(request.productId());
        // 레디스에서 마지막으로 대기열을 나간 사용자 번호 업데이트
        updateLastExitedQueueNumber(request.productId(), request.queueNumber());
        //todo - 해당 상품의 재고 1 마이너스 (미리확보)
        productService.decreaseStockQuantityById(request.productId());
        return;
      }
      Thread.sleep(retryDelay);
    }
    throw new MaxUserLimitExceededException("결제페이지의 최대 사용자 용량에 도달했습니다.");
  }
}
