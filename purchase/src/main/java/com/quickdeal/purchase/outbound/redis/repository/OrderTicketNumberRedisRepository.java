package com.quickdeal.purchase.outbound.redis.repository;

import static com.quickdeal.purchase.outbound.redis.repository.RedisKeyUtils.getLastTicketNumberKey;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderTicketNumberRedisRepository {

  private final RedisTemplate<String, Object> longValueRedisTemplate;

  public OrderTicketNumberRedisRepository(RedisTemplate<String, Object> longValueRedisTemplate) {
    this.longValueRedisTemplate = longValueRedisTemplate;
  }

  public Long increaseLastTicketNumber(Long productId) {
    String key = getLastTicketNumberKey(productId);
    Object value = longValueRedisTemplate.opsForValue().increment(key, 1);
    return value != null ? Long.parseLong(String.valueOf(value)) : 1L;
  }

  public void decreaseTicketNumber(Long productId) {
    String key = getLastTicketNumberKey(productId);
    longValueRedisTemplate.opsForValue().increment(key, -1);
  }

}
