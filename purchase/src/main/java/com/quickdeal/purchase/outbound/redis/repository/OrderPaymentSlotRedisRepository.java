package com.quickdeal.purchase.outbound.redis.repository;

import static com.quickdeal.purchase.outbound.redis.repository.OrderPaymentSlotConstants.UPDATE_LAST_PROCESSED_TICKET_AND_ADD_PAYMENT_USERS;
import static com.quickdeal.purchase.outbound.redis.repository.RedisKeyUtils.getLastExitedTicketNumberKey;
import static com.quickdeal.purchase.outbound.redis.repository.RedisKeyUtils.getPaymentPageUserKey;

import com.quickdeal.purchase.domain.PaymentPageUser;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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

  public boolean updateLastProcessedAndAddPaymentPageUsers(List<PaymentPageUser> requests,
      long productId) {
    String paymentPageAccessUserKey = getPaymentPageUserKey(productId);
    String lastExitedTicketNumberKey = getLastExitedTicketNumberKey(productId);

    long currentTimeInSeconds = Instant.now().getEpochSecond();
    long expiredAtEpochSeconds = currentTimeInSeconds + timeoutInSeconds.getSeconds();

    List<String> userArgs = requests.stream()
        .flatMap(paymentPageUser -> Stream.of(paymentPageUser.userId(),
            String.valueOf(paymentPageUser.ticketNumber())))
        .toList();

    List<String> combinedArgsList = new ArrayList<>();
    combinedArgsList.add(String.valueOf(expiredAtEpochSeconds)); // 만료 시간 추가
    combinedArgsList.addAll(userArgs); // 사용자 데이터 추가

    Object[] args = combinedArgsList.toArray(new Object[0]);

    Optional<Long> result = Optional.ofNullable(
        sortedSetValueRedisTemplate.execute(
            UPDATE_LAST_PROCESSED_TICKET_AND_ADD_PAYMENT_USERS,
            List.of(paymentPageAccessUserKey, lastExitedTicketNumberKey), // keys
            args
        )
    );

    log.debug("[insertPaymentAccessAndUpdateLastProcessed] productId: {}, result: {}", productId,
        result);
    return result.map(value -> value == 1).orElse(false);
  }

  public Long getPaymentAccessUserCount(Long productId) {
    String key = getPaymentPageUserKey(productId);
    return sortedSetValueRedisTemplate.opsForZSet().zCard(key);
  }

  public Long getLastExitedTicketNumber(Long productId) {
    String key = getLastExitedTicketNumberKey(productId);
    Object value = sortedSetValueRedisTemplate.opsForValue().get(key);
    log.debug("[getLastExitedTicketNumber] userId: {}, value: {}", productId, value);
    return value != null ? Long.parseLong(String.valueOf(value)) : 0L;
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
