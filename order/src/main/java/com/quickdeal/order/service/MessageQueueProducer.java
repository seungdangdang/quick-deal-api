package com.quickdeal.order.service;

import com.quickdeal.order.domain.QueueMessage;

public interface MessageQueueProducer {
  void publishMessage(String topic, QueueMessage message);
}
