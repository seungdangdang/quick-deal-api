package com.quickdeal.order.service;

import com.quickdeal.order.api.resource.QueuePollingCommand;
import com.quickdeal.order.config.RedisConfig;
import com.quickdeal.order.service.domain.QueuePolling;
import com.quickdeal.order.util.JWTUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

  private final RedisTemplate<String, String> redisTemplate;
  private final JWTUtil jwtUtil;

  public QueueService(RedisTemplate<String, String> redisTemplate, JWTUtil jwtUtil) {
    this.redisTemplate = redisTemplate;
    this.jwtUtil = jwtUtil;
  }

  // 레디스에서 마지막으로 요청 들어온 대기번호 업데이트하는 코드
  public Long getNewQueueNumber(Long productId) {
    String key = RedisConfig.getLastQueueNumberKey(productId);
    Long newQueueNumber = redisTemplate.opsForValue().increment(key, 1);
    return newQueueNumber != null ? newQueueNumber : 1L;
  }

  // 레디스에서 마지막 요청 들어온 대기번호 가져오는 코드
  public Long getLastQueueNumber(Long productId) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    String value = redisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(value) : 0L;
  }

  // 레디스에서 마지막으로 대기열에서 빠져나간 대기번호 가져오는 코드
  public Long getLastExitedQueueNumber(Long productId) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    String value = redisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(value) : 0L;
  }

  public void updateLastExitedQueueNumber(Long productId, Long lastExitedQueueNumber) { // payment 서비스에서 업데이트할 것
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    redisTemplate.opsForValue().set(key, lastExitedQueueNumber.toString());
  }

  public Long getCurrentPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    String value = redisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(value) : 0L;
  }

  public void incrementPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    redisTemplate.opsForValue().increment(key, 1);
  }

  public void decrementPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    redisTemplate.opsForValue().increment(key, -1);
  }

  public QueuePolling checkQueueStatus(QueuePollingCommand queuePollingCommand) {
    jwtUtil.validateToken(
        queuePollingCommand.jwtToken()); // Q - 토큰서비스에서 간접적으로 사용할지, 직접 사용할지 고려할 것

    Long lastExitedQueueNumber = getLastExitedQueueNumber(queuePollingCommand.productId());
    Long requestQueueNumber = queuePollingCommand.queueNumber();
    long remainingInQueue = requestQueueNumber - lastExitedQueueNumber;

    if (remainingInQueue <= 0) {
      return new QueuePolling(true, 0L, null);
    } else {
      String newToken = jwtUtil.extendQueueJwtExpiration(queuePollingCommand.jwtToken(),
          3600 * 1000L);
      return new QueuePolling(false, remainingInQueue, newToken);
    }
  }
}
