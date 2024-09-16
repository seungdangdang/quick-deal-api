package com.quickdeal.purchase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    // docker 를 위해 설정
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("redis", 6379);
    return new LettuceConnectionFactory(config);  }

  @Bean
  public RedisTemplate<String, Object> objectValueRedisTemplate(
      RedisConnectionFactory connectionFactory
  ) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());

    return template;
  }
}
