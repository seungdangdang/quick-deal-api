package com.quickdeal.order.service;

import com.quickdeal.order.domain.QueueMessage;

public interface MessageQueueService {
  void publishMessage(String topic, QueueMessage message);
}
