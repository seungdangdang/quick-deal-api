package com.quickdeal.scheduler.job;

import io.lettuce.core.api.StatefulConnection;
import java.util.Map;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisStatusScheduler {

  private final Logger log;
  private final Map<Class<?>, GenericObjectPool<StatefulConnection<?, ?>>> pools;

  public RedisStatusScheduler(RedisConnectionFactory connectionFactory) {
    this.log = LoggerFactory.getLogger(RedisStatusScheduler.class);
    this.pools = getConnectionPools(connectionFactory);
  }

  private Map<Class<?>, GenericObjectPool<StatefulConnection<?, ?>>> getConnectionPools(RedisConnectionFactory connectionFactory) {
    try {
      LettuceConnectionProvider proxy = (LettuceConnectionProvider) FieldUtils.readField(
          connectionFactory, "connectionProvider", true);
      LettuceConnectionProvider real = (LettuceConnectionProvider) FieldUtils.readField(proxy,
          "delegate", true);
      return (Map<Class<?>, GenericObjectPool<StatefulConnection<?, ?>>>) FieldUtils.readField(real,
          "pools", true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Async
  @Scheduled(fixedDelay = 1000L)
  void checkStatusWithScheduled() {
    GenericObjectPool<StatefulConnection<?, ?>> o = pools.get(StatefulConnection.class);
    if (o != null) {
      log.info(
          "[checkStatusWithScheduled] numActive: {}, numIdle: {}, numWaiters: {}, borrowedCount: {}, "
          + "createdCount: {}, destroyedCount: {}, allObjectsSize: {}",
          o.getNumActive(), o.getNumIdle(), o.getNumWaiters(), o.getBorrowedCount(),
          o.getCreatedCount(), o.getDestroyedCount(), o.listAllObjects().size());
    }
  }
}
