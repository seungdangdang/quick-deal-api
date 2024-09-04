package com.quickdeal.purchase.service;

import com.quickdeal.purchase.config.RedisConfig;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class RedisService {

  private final Logger log;
  private final RedisTemplate<String, Long> longValueRedisTemplate;
  private final RedisTemplate<String, String> stringValueRedisTemplate;
  private final Jedis jedis;

  public RedisService(RedisTemplate<String, Long> redisTemplate,
      RedisTemplate<String, String> stringValueRedisTemplate, Jedis jedis) {
    this.longValueRedisTemplate = redisTemplate;
    this.stringValueRedisTemplate = stringValueRedisTemplate;
    this.jedis = jedis;
    this.log = LoggerFactory.getLogger(RedisService.class);
  }

  public Object executeLuaScript(String script, List<String> keys, List<String> args) {
    return jedis.eval(script, keys, args);
  }

  // 레디스에서 마지막으로 요청 들어온 대기번호 업데이트하는 코드
  public Long getNewTicketNumber(Long productId) {
    return incrementLastTicketNumber(productId);
  }

  public Long incrementLastTicketNumber(Long productId) {
    String key = RedisConfig.getLastTicketNumberKey(productId);
    Object value = longValueRedisTemplate.opsForValue().increment(key, 1);
    return value != null ? Long.parseLong(String.valueOf(value)) : 1L;
  }

  public void decrementLastTicketNumber(Long productId) {
    String key = RedisConfig.getLastTicketNumberKey(productId);
    longValueRedisTemplate.opsForValue().increment(key, -1);
  }

  // 레디스에서 마지막으로 대기열에서 빠져나간 대기번호 가져오는 코드
  public Long getLastExitedQueueNumber(Long productId) {
    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
    Object value = longValueRedisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(String.valueOf(value)) : 0L;
  }

  //TODO: lua 로 구현할지 고민 중
//  public void updateLastExitedTicketNumber(Long productId, Long lastExitedQueueNumber) {
//    String key = RedisConfig.getLastExitedQueueNumberKey(productId);
//    longValueRedisTemplate.opsForValue().set(key, lastExitedQueueNumber);
//  }

//  //TODO: lua 로 구현할지 고민 중
//  public void incrementPaymentPageUserCount(Long productId) {
//    String key = RedisConfig.getPaymentPageUserKey(productId);
//    stringValueRedisTemplate.opsForValue().increment(key, 1L);
//  }

  public void removePaymentPageUser(Long productId, String userId) {
    String key = RedisConfig.getPaymentPageUserKey(productId);
    stringValueRedisTemplate.opsForSet().remove(key, userId);
  }
}
