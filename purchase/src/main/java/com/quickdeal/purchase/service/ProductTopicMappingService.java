package com.quickdeal.purchase.service;

import com.quickdeal.purchase.config.OrderCreationProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties({OrderCreationProperties.class})
public class ProductTopicMappingService {

  private final Map<Long, String> productIdToTopic;

  // TODO: mock으로 되어 있으나, 고도화 시점에 개선 필요 (당장은 1~20번의 id를 갖는 프로덕트만 처리하는 것으로)
  public ProductTopicMappingService(OrderCreationProperties orderCreationProperties) {
    List<String> topics = orderCreationProperties.getKafkaTopicsOrderCreationRequest();
    this.productIdToTopic = new HashMap<>();
    productIdToTopic.put(1L, topics.get(0));
    productIdToTopic.put(2L, topics.get(1));
    productIdToTopic.put(3L, topics.get(2));
    productIdToTopic.put(4L, topics.get(3));
    productIdToTopic.put(5L, topics.get(4));
    productIdToTopic.put(6L, topics.get(5));
    productIdToTopic.put(7L, topics.get(6));
    productIdToTopic.put(8L, topics.get(7));
    productIdToTopic.put(9L, topics.get(8));
    productIdToTopic.put(10L, topics.get(9));
    productIdToTopic.put(11L, topics.get(10));
    productIdToTopic.put(12L, topics.get(11));
    productIdToTopic.put(13L, topics.get(12));
    productIdToTopic.put(14L, topics.get(13));
    productIdToTopic.put(15L, topics.get(14));
    productIdToTopic.put(16L, topics.get(15));
    productIdToTopic.put(17L, topics.get(16));
    productIdToTopic.put(18L, topics.get(17));
    productIdToTopic.put(19L, topics.get(18));
    productIdToTopic.put(20L, topics.get(19));
  }

  public String getMappedTopicByProductId(Long productId) {
    return productIdToTopic.get(productId);
  }
}
