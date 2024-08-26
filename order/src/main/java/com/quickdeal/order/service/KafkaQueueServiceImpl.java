package com.quickdeal.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.order.domain.QueueMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaQueueServiceImpl implements MessageQueueProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public KafkaQueueServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void publishMessage(String topic, QueueMessage message) {
    try {
      kafkaTemplate.send(topic, objectMapper.writeValueAsString(message));
    } catch (JsonProcessingException e) {
      // todo - 예외처리
    }
  }
}
