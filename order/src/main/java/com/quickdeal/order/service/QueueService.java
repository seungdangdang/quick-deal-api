package com.quickdeal.order.service;

import com.quickdeal.order.api.resource.QueuePollingCommand;
import com.quickdeal.order.config.RedisConfig;
import com.quickdeal.order.domain.QueueStatus;
import io.jsonwebtoken.Claims;
import java.util.Date;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

  private final RedisTemplate<String, Long> redisTemplate;
  private final TokenService tokenService;

  public QueueService(RedisTemplate<String, Long> redisTemplate,
      TokenService tokenService) {
    this.redisTemplate = redisTemplate;
    this.tokenService = tokenService;
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
    Long value = redisTemplate.opsForValue().get(key);
    return value != null ? value : 0L;
  }

  // 레디스에서 마지막으로 대기열에서 빠져나간 대기번호 가져오는 코드
  public Long getLastExitedQueueNumber(Long productId) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    Long value = redisTemplate.opsForValue().get(key);
    return value != null ? value : 0L;
  }

  public void updateLastExitedQueueNumber(Long productId,
      Long lastExitedQueueNumber) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    redisTemplate.opsForValue().set(key, lastExitedQueueNumber);
  }

  public Long getCurrentPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    Long value = redisTemplate.opsForValue().get(key);
    return value != null ? value : 0L;
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

    if (remainingInQueue <= 0) {
      return new QueueStatus(true, 0L, null);
    } else {
      Claims claims = tokenService.getClaimsByToken(queuePollingCommand.jwtToken());
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
}
