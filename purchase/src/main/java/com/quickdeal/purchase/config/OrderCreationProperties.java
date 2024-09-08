package com.quickdeal.purchase.config;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order-creation")
@Getter
@Setter
public class OrderCreationProperties {

  private long retryDelay;
  private long retryLimit;
  private int maxConcurrentUsers;
  private Duration timeoutSeconds;
  private String secretKey;
  private Duration expiration;
  private Duration renewalThreshold;
  private Duration extensionDuration;
  private List<String> kafkaTopicsOrderCreationRequest;
}
