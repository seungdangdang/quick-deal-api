package com.quickdeal.purchase.service;

import com.quickdeal.purchase.config.RedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

  private final Logger log;
  private final RedisTemplate<String, Long> redisTemplate;

  public RedisService(RedisTemplate<String, Long> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.log = LoggerFactory.getLogger(RedisService.class);
  }

  // 레디스에서 마지막으로 요청 들어온 대기번호 업데이트하는 코드
  public Long getNewTicketNumber(Long productId) {
    return incrementLastTicketNumber(productId);
  }

  public Long incrementLastTicketNumber(Long productId) {
    String key = RedisConfig.getLastTicketNumberKey(productId);
    Object value = redisTemplate.opsForValue().increment(key, 1);
    return value != null ? Long.parseLong(String.valueOf(value)) : 1L;
  }

  public void decrementLastTicketNumber(Long productId) {
    String key = RedisConfig.getLastTicketNumberKey(productId);
    redisTemplate.opsForValue().increment(key, -1);
  }

  // 레디스에서 마지막으로 대기열에서 빠져나간 대기번호 가져오는 코드
  public Long getLastExitedQueueNumber(Long productId) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    Object value = redisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(String.valueOf(value)) : 0L;
  }

  public void updateLastExitedTicketNumber(Long productId, Long lastExitedQueueNumber) {
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
    redisTemplate.opsForValue().increment(key, 1L);
  }

  public void decrementPaymentPageUserCount(Long productId) {
    String key = RedisConfig.getPaymentPageUserCountKey(productId);
    redisTemplate.opsForValue().increment(key, -1L);

  }
}
