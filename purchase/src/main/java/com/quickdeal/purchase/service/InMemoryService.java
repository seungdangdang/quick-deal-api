package com.quickdeal.purchase.service;

import com.quickdeal.purchase.config.OrderCreationProperties;
import com.quickdeal.purchase.domain.PaymentPageUser;
import com.quickdeal.purchase.outbound.redis.repository.OrderPaymentSlotRedisRepository;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties({OrderCreationProperties.class})
public class InMemoryService {

  private final OrderPaymentSlotRedisRepository orderPaymentSlotRedisRepository;
  private final int maxPaymentPageUsers;

  public InMemoryService(
      OrderPaymentSlotRedisRepository orderPaymentSlotRedisRepository,
      OrderCreationProperties orderCreationProperties
  ) {
    this.orderPaymentSlotRedisRepository = orderPaymentSlotRedisRepository;
    this.maxPaymentPageUsers = orderCreationProperties.getMaxConcurrentUsers();
  }

  public Long getNumberOfExistsPaymentSlot(long productId) {
    // 결제 슬롯에 들어갈 수 있는 수
    return maxPaymentPageUsers - orderPaymentSlotRedisRepository.getPaymentAccessUserCount(
        productId);
  }

  public boolean insertPaymentAccessAndUpdateLastProcessed(List<PaymentPageUser> requests,
      long productId) {
    return orderPaymentSlotRedisRepository.updateLastProcessedAndAddPaymentPageUsers(
        requests,
        productId
    );
  }
}
