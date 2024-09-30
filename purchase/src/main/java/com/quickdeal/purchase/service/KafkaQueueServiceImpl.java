package com.quickdeal.purchase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.purchase.domain.QueueMessage;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class KafkaQueueServiceImpl implements MessageQueueProducer {

  private final Logger log;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public KafkaQueueServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = new ObjectMapper();
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  @Override
  public void publishMessage(String topic, QueueMessage message) {
    String messageString;
    try {
      messageString = objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    log.info("[publishMessage] message with userId: {}", message.userId());

    CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
        topic,
        message.userId(),
        messageString
    );

    future.thenAccept(result -> {
      log.info(
          "[publishMessage] Message sent successfully to topic: {}, partition: {}, offset: {}, message: {}",
          result.getRecordMetadata().topic(),
          result.getRecordMetadata().partition(),
          result.getRecordMetadata().offset(),
          message);
    });
    future.exceptionally(ex -> {
      log.error("[publishMessage] Failed to send message to topic: {}, message: {}", topic, message, ex);
      return null;
    });
  }
}