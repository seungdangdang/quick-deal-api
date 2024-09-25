package com.quickdeal.purchase.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  private final String redisHost;
  private final int redisPort;

  public RedisConfig(
      @Value("${spring.data.redis.host}") String redisHost,
      @Value("${spring.data.redis.port}") int redisPort
  ) {
    this.redisHost = redisHost;
    this.redisPort = redisPort;
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost,
        redisPort);

    GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();
    poolConfig.setMaxTotal(200);
    poolConfig.setMaxIdle(200);
    poolConfig.setMinIdle(150);

    LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
        .poolConfig(poolConfig)
        .build();

    LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisConfig,
        clientConfig);
    lettuceConnectionFactory.setShareNativeConnection(false); // connection pool을 위해 커넥션공유 false
    return lettuceConnectionFactory;
  }

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
