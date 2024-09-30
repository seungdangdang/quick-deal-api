package com.quickdeal.purchase.service;

import com.quickdeal.purchase.domain.QueueMessage;

public interface MessageQueueProducer {

  void publishMessage(String topic, QueueMessage message);
}
