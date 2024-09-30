package com.quickdeal.purchase.outbound.redis.repository;

import static com.quickdeal.purchase.outbound.redis.repository.OrderPaymentSlotConstants.EXISTS_PAYMENT_SLOT_LUA_SCRIPT;
import static com.quickdeal.purchase.outbound.redis.repository.RedisKeyUtils.getLastExitedTicketNumberKey;
import static com.quickdeal.purchase.outbound.redis.repository.RedisKeyUtils.getPaymentPageUserKey;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderPaymentSlotRedisRepository {

  private final Logger log;
  private final Duration timeoutInSeconds;
  private final RedisTemplate<String, Object> sortedSetValueRedisTemplate;

  public OrderPaymentSlotRedisRepository(
      @Value("${order-creation.timeout-seconds}") Duration timeoutInSeconds,
      RedisTemplate<String, Object> sortedSetValueRedisTemplate
  ) {
    this.log = LoggerFactory.getLogger(OrderPaymentSlotRedisRepository.class);
    this.timeoutInSeconds = timeoutInSeconds;
    this.sortedSetValueRedisTemplate = sortedSetValueRedisTemplate;
  }

  public boolean existsPaymentSlot(
      long productId,
      long ticketNumber,
      String userId,
      int maxConcurrentPaymentUsers
  ) {
    long currentTimeInSeconds = Instant.now().getEpochSecond();
    long expiredAtEpochSeconds = currentTimeInSeconds + timeoutInSeconds.getSeconds();

    Optional<Long> result = Optional.ofNullable(
        sortedSetValueRedisTemplate.execute(
            EXISTS_PAYMENT_SLOT_LUA_SCRIPT,
            List.of(productId + ""),
            ticketNumber + "",
            userId,
            maxConcurrentPaymentUsers + "",
            expiredAtEpochSeconds + ""
        )
    );
    log.debug("[existsPaymentSlot] userId: {}, ticketNumber: {}, result: {}", userId, ticketNumber,
        result);
    return result.map(value -> value == 1)
        .orElse(false);
  }

  public Long getLastExitedTicketNumber(Long productId) {
    String key = getLastExitedTicketNumberKey(productId);
    Object value = sortedSetValueRedisTemplate.opsForValue().get(key);
    log.debug("[getLastExitedTicketNumber] userId: {}, value: {}", productId, value);
    return value != null ? Long.parseLong(String.valueOf(value)) : 0L;
  }

  public void updateLastExitedTicketNumber(Long productId, Long lastExitedTicketNumber) {
    String key = getLastExitedTicketNumberKey(productId);
    log.debug("[updateLastExitedTicketNumber] userId: {}, lastExitedTicketNumber: {}", productId,
        lastExitedTicketNumber);
    sortedSetValueRedisTemplate.opsForValue().set(key, lastExitedTicketNumber);
  }

  public void removePaymentPageUser(Long productId, String userId) {
    String key = getPaymentPageUserKey(productId);
    // TODO: Optional 사용
    Long removedCount = sortedSetValueRedisTemplate.opsForZSet().remove(key, userId);
    log.debug("[removePaymentPageUser] userId: {}, removedCount: {}, key: {}", userId, removedCount,
        key);
    if (removedCount != null && removedCount > 0) {
      log.debug("[removePaymentPageUser] Successfully removed userId: {}, removedCount: {}", userId,
          removedCount);
    } else {
      log.error("[removePaymentPageUser] Failed to remove userId: {}, removedCount: {}", userId,
          removedCount);
    }
  }

  public Boolean existsInPaymentPageUserSortedSet(Long productId, String userId) {
    return getPaymentPageUserSortedSetScore(productId, userId).isPresent();
  }

  public Optional<Long> getPaymentPageUserSortedSetScore(Long productId, String userId) {
    String paymentPageUserKey = getPaymentPageUserKey(productId);
    Optional<Double> fetchedScore = Optional.ofNullable(
        sortedSetValueRedisTemplate.opsForZSet().score(paymentPageUserKey,
            userId));
    log.debug(
        "[getUserExpirationTime] successfully fetched from sorted set. key: {}, valueForSearch(userId): {}, fetchedValue: {}",
        paymentPageUserKey, userId, fetchedScore);
    return fetchedScore.map(score -> BigDecimal.valueOf(score).longValueExact());
  }
}
