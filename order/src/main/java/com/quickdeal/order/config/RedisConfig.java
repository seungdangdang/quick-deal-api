package com.quickdeal.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  public static String getLastQueueNumberKey(Long productId) {
    return "product:" + productId + ":lastQueueNumber";
  }

  public static String getLastExitedQueueNumberKey(Long productId) {
    return "product:" + productId + ":lastExitedQueueNumber";
  }

  public static String getPaymentPageUserCountKey(Long productId) {
    return "product:" + productId + ":paymentPageUserCount";
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    return redisTemplate;
  }
}
