package com.quickdeal.purchase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.Jedis;

@Configuration
public class RedisConfig {

  public static String getLastTicketNumberKey(Long productId) {
    return "product:" + productId + ":lastTicketNumber";
  }

  public static String getLastExitedTicketNumberKey(Long productId) {
    return "product:" + productId + ":lastExitedTicketNumber";
  }

  public static String getPaymentPageUserKey(Long productId) {
    return "product:" + productId + ":paymentPageUser";
  }

  @Bean
  public Jedis jedis() {
    return new Jedis("localhost", 6379);
  }

  @Bean
  public RedisTemplate<String, Long> longValueRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Long> longRedisTemplate = new RedisTemplate<>();
    longRedisTemplate.setConnectionFactory(connectionFactory);
    longRedisTemplate.setKeySerializer(new StringRedisSerializer());
    longRedisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return longRedisTemplate;
  }

  @Bean
  public RedisTemplate<String, String> stringValueRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> stringvalueRedisTemplate = new RedisTemplate<>();
    stringvalueRedisTemplate.setConnectionFactory(connectionFactory);
    stringvalueRedisTemplate.setKeySerializer(new StringRedisSerializer());
    stringvalueRedisTemplate.setValueSerializer(new StringRedisSerializer());
    return stringvalueRedisTemplate;
  }
}
