package com.quickdeal.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.order.api.resource.QueuePollingCommand;
import com.quickdeal.order.config.RedisConfig;
import com.quickdeal.order.domain.QueueMessage;
import com.quickdeal.order.domain.QueueStatus;
import io.jsonwebtoken.Claims;
import java.util.Date;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

  private final RedisTemplate<String, Long> redisTemplate;
  private final TokenService tokenService;

  private static final int MAX_PAYMENT_PAGE_USERS = 10;
  private final ObjectMapper objectMapper;

  public QueueService(RedisTemplate<String, Long> redisTemplate,
      TokenService tokenService, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.tokenService = tokenService;
    this.objectMapper = objectMapper;
  }

  // 레디스에서 마지막으로 요청 들어온 대기번호 업데이트하는 코드
  public Long getNewQueueNumber(Long productId) {
    String key = RedisConfig.getLastQueueNumberKey(productId);
    Object newQueueNumber = redisTemplate.opsForValue().increment(key, 1);
    return newQueueNumber != null ? Long.parseLong(String.valueOf(newQueueNumber)) : 1L;
  }

  // 레디스에서 마지막 요청 들어온 대기번호 가져오는 코드
  public Long getLastQueueNumber(Long productId) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    Object value = redisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(String.valueOf(value)) : 0L;
  }

  // 레디스에서 마지막으로 대기열에서 빠져나간 대기번호 가져오는 코드
  public Long getLastExitedQueueNumber(Long productId) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    Object value = redisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(String.valueOf(value)) : 0L;
  }

  public void updateLastExitedQueueNumber(Long productId,
      Long lastExitedQueueNumber) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    redisTemplate.opsForValue().set(key, lastExitedQueueNumber);
  }

  public Integer getCurrentPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    Object value = redisTemplate.opsForValue().get(key);
    return value != null ? Integer.parseInt(String.valueOf(value)) : 0;
  }

  public void incrementPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    redisTemplate.opsForValue().increment(key, 1);
  }

  public void decrementPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    redisTemplate.opsForValue().increment(key, -1);
  }

  public QueueStatus getUpdatedQueueStatus(QueuePollingCommand queuePollingCommand) {
    Long lastExitedQueueNumber = getLastExitedQueueNumber(queuePollingCommand.productId());
    Long requestQueueNumber = queuePollingCommand.queueNumber();
    long remainingInQueue = requestQueueNumber - lastExitedQueueNumber;

    Claims claims = tokenService.getClaimsByToken(queuePollingCommand.jwtToken());

    if (remainingInQueue <= 0) {
      return new QueueStatus(true, 0L, null);
    } else {
      Date expiration = claims.getExpiration();
      long now = System.currentTimeMillis();
      long timeUntilExpiration = expiration.getTime() - now;

      if (timeUntilExpiration <= 1000L * 60L * 30L) {
        String newToken = tokenService.extendQueueJwtExpiration(queuePollingCommand.jwtToken(),
            1000L * 60L * 60L);
        return new QueueStatus(false, remainingInQueue, newToken);
      } else {
        return new QueueStatus(false, remainingInQueue,
            queuePollingCommand.jwtToken());
      }
    }
  }

  public void processQueueMessage(String message) throws Exception {
    // 메시지를 QueueMessage 객체로 변환
    QueueMessage queueMessage = objectMapper.readValue(message, QueueMessage.class);
    Long productId = queueMessage.productId();

    // 현재 결제 페이지에 있는 사용자 수를 가져옴
    Integer currentCount = getCurrentPaymentPageUserCount(productId);

    // 사용자 수가 최대값보다 적은지 확인
    if (currentCount < MAX_PAYMENT_PAGE_USERS) {
      // 사용자 수 증가
      incrementPaymentPageUserCount(productId);
      // 마지막으로 대기열을 나간 사용자 번호 업데이트
      updateLastExitedQueueNumber(productId, queueMessage.queueNumber());
    } else {
      // 최대 사용자 수를 초과하면 예외 발생 todo - 커스텀 예외 적용
      throw new Exception();
    }
  }
}
